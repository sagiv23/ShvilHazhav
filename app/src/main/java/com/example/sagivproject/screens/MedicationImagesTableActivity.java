package com.example.sagivproject.screens;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An admin activity for managing the table of medication images used in the memory game.
 * <p>
 * This screen allows administrators to view, search, add, and delete medication images.
 * Images are displayed in a grid layout. When an image is deleted, the remaining images are
 * re-ordered to maintain a sequential naming convention (e.g., card1, card2, ...).
 * </p>
 */
@AndroidEntryPoint
public class MedicationImagesTableActivity extends BaseActivity {
    private final List<ImageData> allImages = new ArrayList<>();
    private MedicationImagesTableAdapter adapter;
    private TextInputEditText etSearch;
    private ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> photoPickerLauncher;

    /**
     * Initializes the activity, sets up the UI, RecyclerView, search functionality, and image picker.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_images_table);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.medicationImagesTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        RecyclerView recyclerView = findViewById(R.id.recycler_MedicineImagesTablePage);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = adapterService.getMedicationImagesTableAdapter();
        adapter.setListener(new MedicationImagesTableAdapter.OnImageActionListener() {
                                @Override
                                public void onDeleteImage(ImageData image) {
                                    deleteImageAndReorder(image);
                                }

                                @Override
                                public void onImageClicked(ImageData image, ImageView imageView) {
                                    Drawable drawable = imageView.getDrawable();
                                    if (drawable == null) return;

                                    dialogService.showFullImageDialog(drawable);
                                }
                            }
        );

        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.edit_MedicineImagesTablePage_search);
        Button btnAdd = findViewById(R.id.btn_MedicineImagesTablePage_add);

        btnAdd.setOnClickListener(v -> photoPickerLauncher.launch(
                new androidx.activity.result.PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterImages(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        uploadImage(uri);
                    }
                }
        );

        loadImages();
    }

    /**
     * Fetches all medication images from the database and populates the local list.
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
            public void onFailed(Exception e) {
                Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filters the displayed images based on a search query.
     *
     * @param query The text to search for in the image IDs.
     */
    private void filterImages(String query) {
        List<ImageData> newList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        for (ImageData img : allImages) {
            if (img.getId() != null && img.getId().toLowerCase().contains(lowerQuery)) {
                newList.add(img);
            }
        }

        adapter.setImages(newList);
    }

    /**
     * Uploads a new image selected by the user from their device.
     * The image is converted to Base64 and assigned a new sequential ID.
     *
     * @param uri The URI of the selected image.
     */
    private void uploadImage(Uri uri) {
        try {
            Bitmap bitmap;
            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            ImageView tempIv = new ImageView(this);
            tempIv.setImageBitmap(bitmap);
            String base64 = imageUtil.convertTo64Base(tempIv);

            if (base64 != null) {
                int nextNumber = allImages.size() + 1;
                String newId = "card" + nextNumber;

                ImageData newImg = new ImageData(newId, base64);
                databaseService.getImageService().createImage(newImg, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(MedicationImagesTableActivity.this, "התמונה נוספה כ-" + newId, Toast.LENGTH_SHORT).show();
                        allImages.add(newImg);
                        filterImages(Objects.requireNonNull(etSearch.getText()).toString());
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException e) {
            Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes an image from the database and then triggers the re-ordering of the remaining images.
     *
     * @param imageToDelete The image to be deleted.
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
            public void onFailed(Exception e) {
                Toast.makeText(MedicationImagesTableActivity.this, "שגיאה במחיקת התמונה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Re-orders the IDs of all remaining images to maintain a gapless sequence (e.g., card1, card2, ...).
     * This method is called after an image has been successfully deleted.
     */
    private void reorderImages() {
        for (int i = 0; i < allImages.size(); i++) {
            allImages.get(i).setId("card" + (i + 1));
        }

        databaseService.getImageService().updateAllImages(allImages, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(MedicationImagesTableActivity.this, "הרשימה סודרה מחדש", Toast.LENGTH_SHORT).show();
                filterImages(Objects.requireNonNull(etSearch.getText()).toString());
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בעדכון הרשימה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
