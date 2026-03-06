package com.example.sagivproject.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.sagivproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A custom view that draws a simple XY graph with axes, points, titles, and an optional trend line.
 * Supports string labels for the X-axis (e.g., dates).
 */
public class SimpleXYGraphView extends View {
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trendLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float paddingLeft = 180f;
    private final float paddingBottom = 180f;
    private final float paddingTop = 100f;
    private final float paddingRight = 100f;
    private List<Point> points = new ArrayList<>();
    private List<String> xLabels = new ArrayList<>();
    private String title = "";
    private String labelX = "";
    private String labelY = "";

    public SimpleXYGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Typeface textFont = ResourcesCompat.getFont(context, R.font.text_hebrew);

        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(4f);

        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1.5f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{15, 15}, 0));

        pointPaint.setColor(Color.parseColor("#0097A7"));
        pointPaint.setStyle(Paint.Style.FILL);

        trendLinePaint.setColor(Color.parseColor("#0097A7"));
        trendLinePaint.setStrokeWidth(4f);
        trendLinePaint.setStyle(Paint.Style.STROKE);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(35f);
        textPaint.setTypeface(textFont);
    }

    public void setData(List<Point> points, List<String> xLabels, String title, String labelX, String labelY) {
        this.points = points != null ? points : new ArrayList<>();
        this.xLabels = xLabels != null ? xLabels : new ArrayList<>();
        this.title = title;
        this.labelX = labelX;
        this.labelY = labelY;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) {
            drawNoData(canvas);
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float graphWidth = width - paddingLeft - paddingRight;
        float graphHeight = height - paddingTop - paddingBottom;

        float maxX = 0;
        float maxY = 0;
        for (Point p : points) {
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        float displayMaxX = maxX == 0 ? 1 : maxX;
        float displayMaxY = maxY == 0 ? 100 : maxY * 1.2f;

        // Draw Title
        textPaint.setTextSize(48f);
        textPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.headline_hebrew));
        canvas.drawText(title, width / 2 - textPaint.measureText(title) / 2, paddingTop / 1.5f, textPaint);

        // Draw Axis Labels
        textPaint.setTextSize(38f);
        textPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.text_hebrew));
        // Y Label
        canvas.save();
        canvas.rotate(-90, 60, height / 2);
        canvas.drawText(labelY, 60 - textPaint.measureText(labelY) / 2, height / 2, textPaint);
        canvas.restore();
        // X Label
        canvas.drawText(labelX, width / 2 - textPaint.measureText(labelX) / 2, height - 30, textPaint);

        // Draw Grid and Scale
        drawGrid(canvas, displayMaxX, displayMaxY, graphWidth, graphHeight, height);

        // Draw Axes
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, axisPaint); // X
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, axisPaint); // Y

        // Draw Trend Line
        drawTrendLine(canvas, displayMaxX, displayMaxY, graphWidth, graphHeight, height);

        // Draw Data Points
        for (Point p : points) {
            float px = paddingLeft + (p.x / displayMaxX) * graphWidth;
            float py = (height - paddingBottom) - (p.y / displayMaxY) * graphHeight;
            canvas.drawCircle(px, py, 14f, pointPaint);
        }
    }

    private void drawGrid(Canvas canvas, float maxX, float maxY, float graphWidth, float graphHeight, float height) {
        textPaint.setTextSize(32f);

        // Y Axis
        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            float yVal = (maxY / ySteps) * i;
            float py = (height - paddingBottom) - (yVal / maxY) * graphHeight;
            canvas.drawLine(paddingLeft, py, paddingLeft + graphWidth, py, gridPaint);
            String label = String.format(Locale.getDefault(), "%.0f", yVal);
            canvas.drawText(label, paddingLeft - textPaint.measureText(label) - 30, py + 12, textPaint);
        }

        // X Axis
        int xSteps = points.size();
        for (int i = 0; i < xSteps; i++) {
            float px = paddingLeft + (points.get(i).x / maxX) * graphWidth;
            canvas.drawLine(px, paddingTop, px, height - paddingBottom, gridPaint);

            if (i < xLabels.size()) {
                String label = xLabels.get(i);
                canvas.save();
                canvas.rotate(-30, px, height - paddingBottom + 60);
                canvas.drawText(label, px - 20, height - paddingBottom + 60, textPaint);
                canvas.restore();
            }
        }
    }

    private void drawTrendLine(Canvas canvas, float maxX, float maxY, float graphWidth, float graphHeight, float viewHeight) {
        if (points.size() < 2) return;

        float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (Point p : points) {
            sumX += p.x;
            sumY += p.y;
            sumXY += p.x * p.y;
            sumX2 += p.x * p.x;
        }

        int n = points.size();
        float denom = (n * sumX2 - sumX * sumX);
        if (denom == 0) return;

        float slope = (n * sumXY - sumX * sumY) / denom;
        float intercept = (sumY - slope * sumX) / n;

        float xStart = points.get(0).x;
        float yStart = slope * xStart + intercept;
        float xEnd = points.get(points.size() - 1).x;
        float yEnd = slope * xEnd + intercept;

        float px1 = paddingLeft + (xStart / maxX) * graphWidth;
        float py1 = (viewHeight - paddingBottom) - (yStart / maxY) * graphHeight;
        float px2 = paddingLeft + (xEnd / maxX) * graphWidth;
        float py2 = (viewHeight - paddingBottom) - (yEnd / maxY) * graphHeight;

        canvas.save();
        canvas.clipRect(paddingLeft, paddingTop, paddingLeft + graphWidth, viewHeight - paddingBottom);
        canvas.drawLine(px1, py1, px2, py2, trendLinePaint);
        canvas.restore();

        textPaint.setTextSize(34f);
        String equation = String.format(Locale.US, "y = %.2fx + %.2f", slope, intercept);
        canvas.drawText(equation, paddingLeft + 40, paddingTop + 60, textPaint);
    }

    private void drawNoData(Canvas canvas) {
        String msg = "אין מספיק נתונים להצגת גרף";
        textPaint.setTextSize(45f);
        canvas.drawText(msg, getWidth() / 2 - textPaint.measureText(msg) / 2, getHeight() / 2, textPaint);
    }

    public static class Point {
        public final float x;
        public final float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
