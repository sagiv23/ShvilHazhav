package com.example.sagivproject.ui;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;

/**
 * A custom {@link TypefaceSpan} that allows applying a specific {@link Typeface} object to a span of text.
 * <p>
 * Standard {@code TypefaceSpan} only supports family names (like "monospace"), whereas this implementation
 * allows for custom font files loaded via {@link androidx.core.content.res.ResourcesCompat}.
 * It carefully preserves existing styles (bold, italic) while applying the new font.
 * </p>
 */
public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;

    /**
     * Constructs a new CustomTypefaceSpan.
     * @param family Unused family name string (inherited from superclass).
     * @param type The custom {@link Typeface} to apply.
     */
    public CustomTypefaceSpan(String family, Typeface type) {
        super(family);
        newType = type;
    }

    /**
     * Applies the custom typeface to a Paint object while maintaining style flags.
     * @param paint The Paint object to modify.
     * @param tf The typeface to apply.
     */
    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }

    /**
     * Updates the draw state of the text.
     * @param ds The TextPaint used for drawing.
     */
    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        applyCustomTypeFace(ds, newType);
    }

    /**
     * Updates the measurement state of the text (e.g., for calculating width).
     * @param paint The TextPaint used for measuring.
     */
    @Override
    public void updateMeasureState(@NonNull TextPaint paint) {
        applyCustomTypeFace(paint, newType);
    }
}