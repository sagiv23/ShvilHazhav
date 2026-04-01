package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Represents an image asset used in the application, primarily for the memory game cards.
 * <p>
 * This class holds the image's unique identifier and its content as a Base64 encoded string.
 * It is used for both storing images in the database and passing image data between screens.
 * </p>
 */
public class ImageData implements Serializable, Idable {
    private String id;
    private String base64;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public ImageData() {
    }

    /**
     * Constructs a new ImageData object with specific ID and content.
     * @param id The unique identifier for the image (e.g., "card1").
     * @param base64 The Base64 encoded string representing the image bitmap.
     */
    public ImageData(String id, String base64) {
        this.id = id;
        this.base64 = base64;
    }

    @Override
    public String getId() { return this.id; }

    @Override
    public void setId(String id) { this.id = id; }

    /**
     * @return The Base64 encoded content of the image.
     */
    public String getBase64() { return base64; }

    public void setBase64(String base64) { this.base64 = base64; }

    @NonNull
    @Override
    public String toString() {
        return "ImageData{" +
                "id='" + id + '\'' +
                '}';
    }
}