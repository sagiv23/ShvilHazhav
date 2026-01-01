package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;

import androidx.core.content.res.ResourcesCompat;

import com.example.sagivproject.R;

public class ProfileImageDialog {
    private final Context context;
    private final boolean hasImage;
    private final ImagePickerListener listener;
    private final Typeface typeface;

    public interface ImagePickerListener {
        void onCamera();
        void onGallery();
        void onDelete();
    }

    public ProfileImageDialog(Context context, boolean hasImage, ImagePickerListener listener) {
        this.context = context;
        this.hasImage = hasImage;
        this.listener = listener;
        this.typeface = ResourcesCompat.getFont(context, R.font.text_hebrew);
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_profile_image);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        // מציאת הרכיבים
        Button btnCamera = dialog.findViewById(R.id.btn_profileImageDialog_camera);
        Button btnGallery = dialog.findViewById(R.id.btn_profileImageDialog_gallery);
        Button btnDelete = dialog.findViewById(R.id.btn_profileImageDialog_delete);

        // הצגת/הסתרת כפתור המחיקה
        btnDelete.setVisibility(hasImage ? View.VISIBLE : View.GONE);

        // הגדרת מאזינים
        btnCamera.setOnClickListener(v -> {
            listener.onCamera();
            dialog.dismiss();
        });

        btnGallery.setOnClickListener(v -> {
            listener.onGallery();
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            listener.onDelete();
            dialog.dismiss();
        });

        dialog.show();
    }
}