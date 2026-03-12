package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog that presents options for changing a user's profile image, implemented as a DialogFragment.
 */
@AndroidEntryPoint
public class ProfileImageDialog extends DialogFragment {
    private boolean hasImage;
    private ImagePickerListener listener;

    @Inject
    public ProfileImageDialog() {
    }

    public void setData(boolean hasImage, ImagePickerListener listener) {
        this.hasImage = hasImage;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_profile_image);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        Button btnCamera = dialog.findViewById(R.id.btn_profileImageDialog_camera);
        Button btnGallery = dialog.findViewById(R.id.btn_profileImageDialog_gallery);
        Button btnDelete = dialog.findViewById(R.id.btn_profileImageDialog_delete);
        Button btnCancel = dialog.findViewById(R.id.btn_profileImageDialog_cancel);

        btnDelete.setVisibility(hasImage ? View.VISIBLE : View.GONE);

        btnCamera.setOnClickListener(v -> {
            if (listener != null) listener.onCamera();
            dismiss();
        });

        btnGallery.setOnClickListener(v -> {
            if (listener != null) listener.onGallery();
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete();
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    public interface ImagePickerListener {
        void onCamera();

        void onGallery();

        void onDelete();
    }
}
