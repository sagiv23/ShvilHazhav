package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import java.util.List;

/**
 * An interface that defines the contract for operations related to game card images.
 * <p>
 * This service manages the repository of images used as card faces in the memory game.
 * It provides methods for retrieving the full image list, uploading new images,
 * and performing batch updates for reordering or mass deletion.
 * </p>
 */
public interface IImageService {
    /**
     * Retrieves all image records stored in the database.
     * @param callback The callback invoked with the list of all available {@link ImageData} objects.
     */
    void getAllImages(DatabaseCallback<List<ImageData>> callback);

    /**
     * Stores a new image record in the database.
     * @param image The {@link ImageData} object containing the Base64 content and ID.
     * @param callback An optional callback invoked upon completion.
     */
    void createImage(@NonNull ImageData image, @Nullable DatabaseCallback<Void> callback);

    /**
     * Replaces the entire image collection in the database with a new list.
     * <p>
     * This method is useful for administrative tasks like reordering IDs or
     * syncing a local state with the database in a single operation.
     * </p>
     * @param list The complete list of {@link ImageData} objects to be saved.
     * @param callback A callback invoked when the batch update is finished.
     */
    void updateAllImages(List<ImageData> list, DatabaseCallback<Void> callback);

    /**
     * Removes a specific image from the database by its identifier.
     * @param imageId The unique identifier of the image to delete.
     * @param callback An optional callback invoked upon completion.
     */
    void deleteImage(@NonNull String imageId, @Nullable DatabaseCallback<Void> callback);
}