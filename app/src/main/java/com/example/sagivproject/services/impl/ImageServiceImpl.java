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
 * This class provides methods for managing the memory game's image assets in the database.
 * It supports retrieving all images, adding new ones, deleting, and batch updating the
 * entire collection (useful for reordering).
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

    @Override
    public void getAllImages(DatabaseCallback<List<ImageData>> callback) {
        super.getAll(callback);
    }

    @Override
    public void createImage(@NonNull ImageData image, @Nullable DatabaseCallback<Void> callback) {
        super.create(image, callback);
    }

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

    @Override
    public void deleteImage(@NonNull String imageId, @Nullable DatabaseCallback<Void> callback) {
        super.delete(imageId, callback);
    }
}
