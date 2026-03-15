package com.example.sagivproject.screens;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationImagesTableAdapter;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.utils.ImageUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An admin-only fragment for managing the set of images used for medication cards in the memory game.
 * <p>
 * This fragment provides a grid view of all currently uploaded images. Administrators can
 * search for specific images by ID, upload new images from the device gallery, and delete existing ones.
 * When an image is deleted, the remaining images are automatically reordered/renamed to maintain consistency.
 * </p>
 */
@AndroidEntryPoint
public class MedicationImagesTableFragment extends BaseFragment {
    private final List<ImageData> allImages = new ArrayList<>();

    /**
     * Utility for image processing and Base64 conversion.
     */
    @Inject
    protected ImageUtil imageUtil;

    private MedicationImagesTableAdapter adapter;
    private TextInputEditText etSearch;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medication_images_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_MedicineImagesTablePage);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = adapterService.getMedicationImagesTableAdapter();
        adapter.setListener(new MedicationImagesTableAdapter.OnImageActionListener() {
            @Override
            public void onDeleteImage(ImageData image) {
                deleteImageAndReorder(image);
            }

            @Override
            public void onImageClicked(ImageData image, ImageView imageView) {
                Drawable drawable = imageView.getDrawable();
                if (drawable != null)
                    dialogService.showFullImageDialog(getParentFragmentManager(), drawable);
            }
        });
        recyclerView.setAdapter(adapter);

        etSearch = view.findViewById(R.id.edit_MedicineImagesTablePage_search);
        Button btnAdd = view.findViewById(R.id.btn_MedicineImagesTablePage_add);

        // Launcher for picking a new image to upload
        photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) uploadImage(uri);
        });

        btnAdd.setOnClickListener(v -> photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

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

        loadImages();
    }

    /**
     * Fetches all images from the database and updates the UI.
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
                Toast.makeText(requireContext(), "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filters the displayed images based on their ID.
     *
     * @param query The search query string.
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
     * Handles the image upload process: decodes the URI, converts to Base64, and saves to database.
     *
     * @param uri The URI of the image to upload.
     */
    private void uploadImage(Uri uri) {
        try {
            if (getContext() == null) return;
            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContext().getContentResolver(), uri));
            ImageView tempIv = new ImageView(getContext());
            tempIv.setImageBitmap(bitmap);
            String base64 = imageUtil.convertTo64Base(tempIv);

            if (base64 != null) {
                String newId = "card" + (allImages.size() + 1);
                ImageData newImg = new ImageData(newId, base64);
                databaseService.getImageService().createImage(newImg, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(requireContext(), "התמונה נוספה כ-" + newId, Toast.LENGTH_SHORT).show();
                        allImages.add(newImg);
                        filterImages(Objects.requireNonNull(etSearch.getText()).toString());
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(requireContext(), "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException ignored) {
            Toast.makeText(requireContext(), "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes an image from the database and triggers a reordering of the remaining images.
     *
     * @param imageToDelete The {@link ImageData} object to delete.
     */
    private void deleteImageAndReorder(ImageData imageToDelete) {
        databaseService.getImageService().deleteImage(imageToDelete.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(requireContext(), "התמונה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
                allImages.remove(imageToDelete);
                reorderImages();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה במחיקת התמונה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Reorders all images in the list, reassigning IDs sequentially (card1, card2, etc.),
     * and updates the database to reflect these changes.
     */
    private void reorderImages() {
        for (int i = 0; i < allImages.size(); i++)
            allImages.get(i).setId("card" + (i + 1));

        databaseService.getImageService().updateAllImages(allImages, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(requireContext(), "הרשימה סודרה מחדש", Toast.LENGTH_SHORT).show();
                filterImages(Objects.requireNonNull(etSearch.getText()).toString());
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה בעדכון הרשימה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
