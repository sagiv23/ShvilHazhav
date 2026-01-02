package com.example.sagivproject.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.ImageUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedicationImagesTableActivity extends BaseActivity {
    Button btnToAdminPage, btnAdd;
    RecyclerView recyclerView;
    private MedicationImagesTableAdapter adapter;
    List<ImageData> allImages = new ArrayList<>();
    List<ImageData> filteredList = new ArrayList<>();
    private TextInputEditText etSearch;

    //פתיחת גלריה לבחירת תמונה
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                uploadImage(imageUri);
            }
        }
    );

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

        btnToAdminPage = findViewById(R.id.btn_MedicineImagesTablePage_to_admin);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(MedicationImagesTableActivity.this, AdminPageActivity.class)));

        recyclerView = findViewById(R.id.recycler_MedicineImagesTablePage);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MedicationImagesTableAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.edit_MedicineImagesTablePage_search);
        btnAdd = findViewById(R.id.btn_MedicineImagesTablePage_add);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterImages(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadImages();
    }

    private void loadImages() {
        databaseService.getAllImages(new DatabaseService.DatabaseCallback<List<ImageData>>() {
            @Override
            public void onCompleted(List<ImageData> list) {
                allImages.clear();
                if (list != null) allImages.addAll(list);
                filterImages(etSearch.getText().toString());
            }
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterImages(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase().trim();
        for (ImageData img : allImages) {
            if (img.getId() != null && img.getId().toLowerCase().contains(lowerQuery)) {
                filteredList.add(img);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void uploadImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ImageView tempIv = new ImageView(this);
            tempIv.setImageBitmap(bitmap);
            String base64 = ImageUtil.convertTo64Base(tempIv);

            if (base64 != null) {
                String newId = databaseService.generateImageId();
                ImageData newImg = new ImageData(newId, base64);
                databaseService.createImage(newImg, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(MedicationImagesTableActivity.this, "התמונה נוספה!", Toast.LENGTH_SHORT).show();
                        loadImages();
                    }
                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(MedicationImagesTableActivity.this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}