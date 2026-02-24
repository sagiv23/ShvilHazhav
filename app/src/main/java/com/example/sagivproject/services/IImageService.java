package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to the memory game images.
 * <p>
 * This service manages the collection of images used as cards in the memory game.
 * </p>
 */
public interface IImageService {
    /**
     * Retrieves all images from the database.
     *
     * @param callback The callback to be invoked with the list of all images.
     */
    void getAllImages(DatabaseCallback<List<ImageData>> callback);

    /**
     * Creates a new image in the database.
     *
     * @param image    The image data to create.
     * @param callback An optional callback to be invoked upon completion.
     */
    void createImage(@NonNull ImageData image, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates the entire collection of images in the database with a new list.
     * <p>
     * This is useful for batch operations like re-ordering or deleting multiple images,
     * as it replaces the entire existing collection with the provided list in a single operation.
     * </p>
     *
     * @param list     The new, complete list of images to be saved.
     * @param callback A callback to be invoked upon completion.
     */
    void updateAllImages(List<ImageData> list, DatabaseCallback<Void> callback);

    /**
     * Deletes a specific image from the database.
     *
     * @param imageId  The ID of the image to delete.
     * @param callback An optional callback to be invoked upon completion.
     */
    void deleteImage(@NonNull String imageId, @Nullable DatabaseCallback<Void> callback);
}
