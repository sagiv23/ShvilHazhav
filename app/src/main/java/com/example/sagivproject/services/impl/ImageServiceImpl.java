package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IImageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * An implementation of the {@link IImageService} interface.
 * <p>
 * This service manages the collection of images used in the memory game. It provides methods
 * to fetch all images, create a new image, and update the entire collection at once.
 * It extends {@link BaseDatabaseService} for basic CRUD operations.
 * </p>
 */
public class ImageServiceImpl extends BaseDatabaseService<ImageData> implements IImageService {
    private static final String IMAGES_PATH = "images";

    /**
     * Constructs a new ImageServiceImpl.
     */
    @Inject
    public ImageServiceImpl() {
        super(IMAGES_PATH, ImageData.class);
    }

    /**
     * Retrieves all images from the database.
     *
     * @param callback The callback to be invoked with the list of images.
     */
    @Override
    public void getAllImages(DatabaseCallback<List<ImageData>> callback) {
        super.getAll(callback);
    }

    /**
     * Creates a new image in the database.
     *
     * @param image    The image data to create.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void createImage(@NonNull ImageData image, @Nullable DatabaseCallback<Void> callback) {
        super.create(image, callback);
    }

    /**
     * Updates the entire collection of images in the database.
     * This is useful for re-ordering or batch-deleting images.
     *
     * @param list     The new list of images to be saved.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void updateAllImages(List<ImageData> list, DatabaseCallback<Void> callback) {
        // Convert the list to a map for a single, efficient write operation.
        Map<String, Object> map = new HashMap<>();
        for (ImageData img : list) {
            map.put(img.getId(), img);
        }

        // Use the protected writeData helper to overwrite the entire images' collection.
        writeData(IMAGES_PATH, map, callback);
    }
}
