package com.example.sagivproject.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;

import com.example.sagivproject.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A utility class for handling image-related operations.
 * <p>
 * This class provides methods for converting images to and from Base64 encoded strings,
 * and for loading images into ImageViews. It is managed as a Singleton by Hilt.
 * </p>
 */
@Singleton
public class ImageUtil {
    private static final String BASE64_PREFIX = "data:image/jpeg;base64,";

    @Inject
    public ImageUtil() {
    }

    /**
     * Converts the drawable from an ImageView into a Base64 encoded string.
     *
     * @param imageView The ImageView containing the image to convert.
     * @return The Base64 encoded string with a data URI prefix, or null if the drawable is not present.
     */
    public @Nullable String convertTo64Base(@NotNull final ImageView imageView) {
        if (imageView.getDrawable() == null || !(imageView.getDrawable() instanceof BitmapDrawable)) {
            return null;
        }
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return BASE64_PREFIX + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Loads a Base64 encoded image into an ImageView.
     *
     * @param base64Code The Base64 encoded string of the image, potentially with a data URI prefix.
     * @param imageView  The ImageView to load the image into.
     */
    public void loadImage(@Nullable final String base64Code, @NotNull final ImageView imageView) {
        if (base64Code == null || base64Code.isEmpty()) {
            // Set a default user icon if no image is available
            imageView.setImageResource(R.drawable.ic_user);
            return;
        }

        try {
            // Remove the data URI prefix if it exists
            String pureBase64 = base64Code.contains(",") ? base64Code.substring(base64Code.indexOf(",") + 1) : base64Code;
            byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_user);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_user);
        }
    }
}
