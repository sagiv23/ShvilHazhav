package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Represents an image used in the application, primarily for the memory game cards.
 * <p>
 * This class holds the image's unique ID and its content as a Base64 encoded string.
 * </p>
 */
public class ImageData implements Serializable, Idable {
    private String id;
    private String base64;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(ImageData.class).
     */
    public ImageData() {
    }

    /**
     * Constructs a new ImageData object.
     *
     * @param id     The unique ID of the image (e.g., "card1").
     * @param base64 The Base64 encoded string of the image.
     */
    public ImageData(String id, String base64) {
        this.id = id;
        this.base64 = base64;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    @NonNull
    @Override
    public String toString() {
        return "ImageData{" +
                "id='" + id + '\'' +
                ", base64='" + base64 + '\'' +
                '}';
    }
}
