package com.example.sagivproject.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.sagivproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A custom view that renders a scrollable XY graph.
 * <p>
 * This view is designed to visualize user performance trends (e.g., game scores or medication adherence).
 * Key features include:
 * <ul>
 *     <li>Custom axes and grid lines with dash effects.</li>
 *     <li>Automatic scaling based on data points.</li>
 *     <li>Horizontal scrolling for long data sets.</li>
 *     <li>Linear regression trend line calculation and display.</li>
 *     <li>Support for custom X-axis labels (e.g., dates).</li>
 * </ul>
 * </p>
 */
public class SimpleXYGraphView extends View {
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trendLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float paddingLeft = 180f;
    private final float paddingBottom = 220f;
    private final float paddingTop = 120f;
    private final float pointSpacing = 200f;

    private List<Point> points = new ArrayList<>();
    private List<String> xLabels = new ArrayList<>();
    private String title = "";
    private String labelX = "";
    private String labelY = "";

    private float scrollXOffset = 0f;
    private float lastTouchX = 0f;

    /**
     * Constructs a new SimpleXYGraphView.
     */
    public SimpleXYGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Initializes the paint objects and styles for drawing the graph.
     */
    private void init(Context context) {
        Typeface textFont = ResourcesCompat.getFont(context, R.font.text_hebrew);
        int textColor = ContextCompat.getColor(context, R.color.text_color);

        axisPaint.setColor(textColor);
        axisPaint.setStrokeWidth(2f);

        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1.5f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{15, 15}, 0));

        pointPaint.setColor(ContextCompat.getColor(context, R.color.error));
        pointPaint.setStyle(Paint.Style.FILL);

        trendLinePaint.setColor(ContextCompat.getColor(context, R.color.error));
        trendLinePaint.setStrokeWidth(3f);
        trendLinePaint.setStyle(Paint.Style.STROKE);

        textPaint.setColor(textColor);
        textPaint.setTextSize(45f);
        textPaint.setTypeface(textFont);
    }

    /**
     * Sets the data to be displayed on the graph.
     *
     * @param points  A list of {@link Point} objects.
     * @param xLabels Labels for the X-axis corresponding to each point.
     * @param title   The graph title.
     * @param labelX  Label for the X-axis.
     * @param labelY  Label for the Y-axis.
     */
    public void setData(List<Point> points, List<String> xLabels, String title, String labelX, String labelY) {
        this.points = points != null ? points : new ArrayList<>();
        this.xLabels = xLabels != null ? xLabels : new ArrayList<>();
        this.title = title;
        this.labelX = labelX;
        this.labelY = labelY;
        this.scrollXOffset = 0;
        invalidate(); // Request a redraw
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Handles touch events to implement horizontal scrolling when data exceeds view width.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (points.size() < 2) return super.onTouchEvent(event);

        float totalWidth = paddingLeft + (points.size() - 1) * pointSpacing + 100f;
        float viewWidth = getWidth();

        if (totalWidth <= viewWidth) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                scrollXOffset += dx;
                // Clamp scroll to boundaries
                if (scrollXOffset > 0) scrollXOffset = 0;
                if (scrollXOffset < viewWidth - totalWidth) scrollXOffset = viewWidth - totalWidth;
                lastTouchX = event.getX();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) {
            drawNoData(canvas);
            return;
        }

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float paddingRight = 100f;
        float graphHeight = viewHeight - paddingTop - paddingBottom;

        // Calculate vertical scale
        float maxY = 0;
        for (Point p : points) {
            maxY = Math.max(maxY, p.y);
        }
        float displayMaxY = maxY == 0 ? 100 : maxY * 1.2f;

        // Draw title
        textPaint.setTextSize(56f);
        textPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.headline_hebrew));
        canvas.drawText(title, viewWidth / 2 - textPaint.measureText(title) / 2, paddingTop / 1.5f, textPaint);

        // Draw labels
        textPaint.setTextSize(50f);
        textPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.text_hebrew));
        canvas.save();
        canvas.rotate(-90, 60, viewHeight / 2);
        canvas.drawText(labelY, 60 - textPaint.measureText(labelY) / 2, viewHeight / 2, textPaint);
        canvas.restore();
        canvas.drawText(labelX, viewWidth / 2 - textPaint.measureText(labelX) / 2, viewHeight - 30, textPaint);

        // --- Start Scrolling Area ---
        canvas.save();
        // Clip content to ensure scrolling doesn't bleed into fixed axis labels
        canvas.clipRect(paddingLeft, 0, viewWidth, viewHeight - paddingBottom + 150);
        canvas.translate(scrollXOffset, 0);

        float totalGraphWidth = Math.max(viewWidth - paddingLeft - paddingRight, (points.size() - 1) * pointSpacing);
        drawGridY(canvas, displayMaxY, totalGraphWidth, graphHeight, viewHeight);

        // Draw X-axis line
        canvas.drawLine(paddingLeft, viewHeight - paddingBottom, paddingLeft + totalGraphWidth, viewHeight - paddingBottom, axisPaint);

        // Draw trend line
        drawTrendLine(canvas, displayMaxY, graphHeight, viewHeight);

        // Draw data points and corresponding X-axis labels
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            float px = paddingLeft + i * pointSpacing;
            float py = (viewHeight - paddingBottom) - (p.y / displayMaxY) * graphHeight;

            // Draw vertical grid line
            canvas.drawLine(px, paddingTop, px, viewHeight - paddingBottom, gridPaint);

            // Draw data point
            canvas.drawCircle(px, py, 16f, pointPaint);

            // Draw rotated X label for better fit
            if (i < xLabels.size()) {
                textPaint.setTextSize(40f);
                String label = xLabels.get(i);
                canvas.save();
                canvas.rotate(-45, px, viewHeight - paddingBottom + 60);
                canvas.drawText(label, px - 40, viewHeight - paddingBottom + 80, textPaint);
                canvas.restore();
            }
        }

        canvas.restore();
        // --- End Scrolling Area ---

        // Draw fixed Y-axis line
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, viewHeight - paddingBottom, axisPaint);

        drawYLabels(canvas, displayMaxY, graphHeight, viewHeight);
    }

    /**
     * Draws horizontal grid lines.
     */
    private void drawGridY(Canvas canvas, float maxY, float totalWidth, float graphHeight, float viewHeight) {
        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            float yVal = (maxY / ySteps) * i;
            float py = (viewHeight - paddingBottom) - (yVal / maxY) * graphHeight;
            canvas.drawLine(paddingLeft, py, paddingLeft + totalWidth, py, gridPaint);
        }
    }

    /**
     * Draws value labels for the Y-axis.
     */
    private void drawYLabels(Canvas canvas, float maxY, float graphHeight, float viewHeight) {
        textPaint.setTextSize(38f);
        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            float yVal = (maxY / ySteps) * i;
            float py = (viewHeight - paddingBottom) - (yVal / maxY) * graphHeight;
            String label = String.format(Locale.getDefault(), "%.0f", yVal);
            canvas.drawText(label, paddingLeft - textPaint.measureText(label) - 30, py + 12, textPaint);
        }
    }

    /**
     * Calculates and draws a linear regression trend line for the provided points.
     */
    private void drawTrendLine(Canvas canvas, float maxY, float graphHeight, float viewHeight) {
        if (points.size() < 2) return;

        float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < points.size(); i++) {
            float y = points.get(i).y;
            sumX += (float) i;
            sumY += y;
            sumXY += (float) i * y;
            sumX2 += (float) i * (float) i;
        }

        int n = points.size();
        float denom = (n * sumX2 - sumX * sumX);
        if (denom == 0) return;

        float slope = (n * sumXY - sumX * sumY) / denom;
        float yStart = (sumY - slope * sumX) / n;

        float xEnd = n - 1;
        float yEnd = slope * xEnd + yStart;

        float py1 = (viewHeight - paddingBottom) - (yStart / maxY) * graphHeight;
        float px2 = paddingLeft + (n - 1) * pointSpacing;
        float py2 = (viewHeight - paddingBottom) - (yEnd / maxY) * graphHeight;

        canvas.drawLine(paddingLeft, py1, px2, py2, trendLinePaint);

        // Render trend equation string
        textPaint.setTextSize(44f);
        textPaint.setFakeBoldText(true);
        String equation = String.format(Locale.US, "y = %.2fx + %.2f", slope, yStart);

        canvas.save();
        // Keep equation visible at top left regardless of scroll
        canvas.translate(-scrollXOffset, 0);
        canvas.drawText(equation, paddingLeft + 40, paddingTop + 60, textPaint);
        textPaint.setFakeBoldText(false);
        canvas.restore();
    }

    /**
     * Draws a placeholder message when no data is available.
     */
    private void drawNoData(Canvas canvas) {
        String msg = "אין מספיק נתונים להצגת הגרף";
        textPaint.setTextSize(45f);
        canvas.drawText(msg, (float) getWidth() / 2 - textPaint.measureText(msg) / 2, (float) getHeight() / 2, textPaint);
    }

    /**
     * Data model for a single point on the graph.
     */
    public static class Point {
        public final float x;
        public final float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
