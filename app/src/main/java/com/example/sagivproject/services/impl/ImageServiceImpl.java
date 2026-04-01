package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IImageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Implementation of the {@link IImageService} interface.
 * <p>
 * This class provides methods for managing the memory game's image assets in the Firebase
 * Realtime Database. It supports retrieving all images, adding new ones, deleting, and
 * batch updating the entire collection, which is particularly useful for reordering IDs
 * sequentially (e.g., card1, card2, etc.).
 * </p>
 */
public class ImageServiceImpl extends BaseDatabaseService<ImageData> implements IImageService {
    /** The database path where game images are stored. */
    private static final String IMAGES_PATH = "images";

    /**
     * Constructs a new ImageServiceImpl.
     * Initializes the base service with the images' path.
     */
    @Inject
    public ImageServiceImpl() { super(IMAGES_PATH, ImageData.class); }

    /**
     * Retrieves all image records from the database.
     * @param callback The callback invoked with the list of all images.
     */
    @Override
    public void getAllImages(DatabaseCallback<List<ImageData>> callback) { super.getAll(callback); }

    /**
     * Creates a new image entry in the database.
     * @param image The {@link ImageData} object to create.
     * @param callback An optional callback to be invoked upon completion.
     */
    @Override
    public void createImage(@NonNull ImageData image, @Nullable DatabaseCallback<Void> callback) { super.create(image, callback); }

    /**
     * Replaces the entire image collection in the database with a provided list.
     * <p>
     * This method converts the list into a map for an atomic overwrite operation,
     * ensuring that reordered IDs are correctly applied across the entire set.
     * </p>
     * @param list The new, complete list of images to be saved.
     * @param callback A callback invoked when the batch update is finished.
     */
    @Override
    public void updateAllImages(List<ImageData> list, DatabaseCallback<Void> callback) {
        Map<String, Object> map = new HashMap<>();
        for (ImageData img : list) {
            map.put(img.getId(), img);
        }

        writeData(IMAGES_PATH, map, callback);
    }

    /**
     * Deletes a specific image from the database by its ID.
     * @param imageId The unique identifier of the image to remove.
     * @param callback An optional callback invoked upon completion.
     */
    @Override
    public void deleteImage(@NonNull String imageId, @Nullable DatabaseCallback<Void> callback) { super.delete(imageId, callback); }
}