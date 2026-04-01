package com.example.sagivproject.screens;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationImagesTableAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.utils.ImageUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Administrative activity for managing the library of medication card images.
 * <p>
 * This screen allows administrators to:
 * <ul>
 * <li>View a grid of all currently uploaded images for the memory game.</li>
 * <li>Search for specific images by their unique ID.</li>
 * <li>Upload new images from the device's storage.</li>
 * <li>Delete existing images, with automatic sequential ID reordering.</li>
 * <li>View full-screen previews of card images.</li>
 * </ul>
 * It ensures the game board has a consistent set of sequentially named IDs (card1, card2, etc.).
 * </p>
 */
@AndroidEntryPoint
public class MedicationImagesTableActivity extends BaseActivity {
    /**
     * Cached list of all images currently in the database.
     */
    private final List<ImageData> allImages = new ArrayList<>();

    /**
     * Utility for image decoding and Base64 conversion.
     */
    @Inject
    protected ImageUtil imageUtil;

    private MedicationImagesTableAdapter adapter;
    private TextInputEditText etSearch;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_images_table);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.medicationImagesTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        RecyclerView recyclerView = findViewById(R.id.recycler_MedicineImagesTablePage);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = adapterService.getMedicationImagesTableAdapter();
        adapter.setListener(new MedicationImagesTableAdapter.OnImageActionListener() {
            @Override
            public void onDeleteImage(ImageData image) { deleteImageAndReorder(image); }

            @Override
            public void onImageClicked(ImageData image, ImageView imageView) {
                Drawable drawable = imageView.getDrawable();
                if (drawable != null)
                    dialogService.showFullImageDialog(getSupportFragmentManager(), drawable);
            }
        });
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.edit_MedicineImagesTablePage_search);
        Button btnAdd = findViewById(R.id.btn_MedicineImagesTablePage_add);

        photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) uploadImage(uri);
        });

        btnAdd.setOnClickListener(v -> checkGalleryPermission());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { filterImages(s.toString()); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadImages();
    }

    /**
     * Validates gallery access permissions before launching the photo picker.
     */
    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    /**
     * Launches the system photo picker.
     */
    private void openGallery() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }

    @Override
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
        if (Boolean.TRUE.equals(isGranted.get(Manifest.permission.READ_MEDIA_IMAGES))) {
            openGallery();
        } else {
            Toast.makeText(this, "נדרשת הרשאת גישה לתמונות כדי להוסיף תמונה", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Synchronizes the local image cache with the Firebase database.
     */
    private void loadImages() {
        databaseService.getImageService().getAllImages(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ImageData> list) {
                allImages.clear();
                if (list != null) allImages.addAll(list);
                filterImages(Objects.requireNonNull(etSearch.getText()).toString());
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Filters the grid display based on the user's ID search query.
     * @param query The search text.
     */
    private void filterImages(String query) {
        List<ImageData> newList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        for (ImageData img : allImages) {
            if (img.getId() != null && img.getId().toLowerCase().contains(lowerQuery))
                newList.add(img);
        }
        adapter.setImages(newList);
    }

    /**
     * Decodes a selected image URI, converts it to Base64, and saves it as a new card record.
     * @param uri The URI of the selected image.
     */
    private void uploadImage(Uri uri) {
        try {
            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            ImageView tempIv = new ImageView(this);
            tempIv.setImageBitmap(bitmap);
            String base64 = imageUtil.convertTo64Base(tempIv);

            if (base64 != null) {
                String newId = "card" + (allImages.size() + 1);
                ImageData newImg = new ImageData(newId, base64);
                databaseService.getImageService().createImage(newImg, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(MedicationImagesTableActivity.this, "התמונה נוספה כ-" + newId, Toast.LENGTH_SHORT).show();
                        allImages.add(newImg);
                        filterImages(Objects.requireNonNull(etSearch.getText()).toString());
                    }

                    @Override
                    public void onFailed(Exception e) { Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show(); }
                });
            }
        } catch (IOException ignored) {
            Toast.makeText(this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes an image from the database and triggers a batch update to re-index IDs.
     * @param imageToDelete The image object to remove.
     */
    private void deleteImageAndReorder(ImageData imageToDelete) {
        databaseService.getImageService().deleteImage(imageToDelete.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(MedicationImagesTableActivity.this, "התמונה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
                allImages.remove(imageToDelete);
                reorderImages();
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(MedicationImagesTableActivity.this, "שגיאה במחיקת התמונה", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Re-assigns sequential IDs (card1, card2, etc.) to all remaining images and syncs with the database.
     */
    private void reorderImages() {
        for (int i = 0; i < allImages.size(); i++)
            allImages.get(i).setId("card" + (i + 1));

        databaseService.getImageService().updateAllImages(allImages, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(MedicationImagesTableActivity.this, "הרשימה סודרה מחדש", Toast.LENGTH_SHORT).show();
                filterImages(Objects.requireNonNull(etSearch.getText()).toString());
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בעדכון הרשימה", Toast.LENGTH_SHORT).show(); }
        });
    }
}