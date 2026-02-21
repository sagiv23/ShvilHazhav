package com.example.sagivproject.ui;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;

/**
 * A custom {@link TypefaceSpan} that allows setting a specific {@link Typeface} object on a span of text.
 * <p>
 * This is used throughout the application to apply custom fonts to parts of a string,
 * such as in menu items or styled text views, while preserving existing styles like bold or italic.
 * </p>
 */
public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;

    /**
     * Constructs a new CustomTypefaceSpan.
     *
     * @param family This parameter is inherited from TypefaceSpan but is not used in this implementation.
     * @param type   The typeface to apply to the text.
     */
    public CustomTypefaceSpan(String family, Typeface type) {
        super(family);
        newType = type;
    }

    /**
     * A helper method to apply the new typeface to a Paint object while preserving old styles.
     *
     * @param paint The Paint object to modify.
     * @param tf    The new typeface to apply.
     */
    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        // Keep any old styles (like bold/italic) that are not part of the new typeface.
        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        applyCustomTypeFace(ds, newType);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint paint) {
        applyCustomTypeFace(paint, newType);
    }
}
