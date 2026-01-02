package com.example.sagivproject.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;

/// Utility class for image operations
/// Contains methods for requesting permissions, converting images to base64 and vice versa
public class ImageUtil {
    /// Convert an image to a base64 string
    /// @param postImage The image to convert
    /// @return The base64 string representation of the image
    public static @Nullable String convertTo64Base(@NotNull final ImageView postImage) {
        if (postImage.getDrawable() == null) {
            return null;
        }
        Bitmap bitmap = ((BitmapDrawable) postImage.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /// Convert a base64 string to an image
    /// @param base64Code The base64 string to convert
    /// @return The image represented by the base64 string
    public static @Nullable Bitmap convertFrom64base(@NotNull final String base64Code) {
        if (base64Code.isEmpty()) {
            return null;
        }
        byte[] decodedString = Base64.decode(base64Code, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}