package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.example.sagivproject.R;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A dialog that presents options for changing a user's profile image.
 * <p>
 * This dialog provides buttons to select a new image from the camera or gallery.
 * It also provides an option to delete the current profile image, if one exists.
 * </p>
 */
@ActivityScoped
public class ProfileImageDialog {
    private final Context context;

    /**
     * Constructs a new ProfileImageDialog.
     * Hilt uses this constructor to provide an instance.
     *
     * @param context The context in which the dialog should be shown.
     */
    @Inject
    public ProfileImageDialog(@ActivityContext Context context) {
        this.context = context;
    }

    /**
     * Creates and displays the dialog.
     *
     * @param hasImage True if the user currently has a profile image, which determines if the delete option is shown.
     * @param listener The listener to be invoked when an option is selected.
     */
    public void show(boolean hasImage, ImagePickerListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_profile_image);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        Button btnCamera = dialog.findViewById(R.id.btn_profileImageDialog_camera);
        Button btnGallery = dialog.findViewById(R.id.btn_profileImageDialog_gallery);
        Button btnDelete = dialog.findViewById(R.id.btn_profileImageDialog_delete);
        Button btnCancel = dialog.findViewById(R.id.btn_profileImageDialog_cancel);

        // Only show the delete button if the user has an image
        btnDelete.setVisibility(hasImage ? View.VISIBLE : View.GONE);

        btnCamera.setOnClickListener(v -> {
            if (listener != null) listener.onCamera();
            dialog.dismiss();
        });

        btnGallery.setOnClickListener(v -> {
            if (listener != null) listener.onGallery();
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * An interface for listeners that are invoked when an image source option is selected.
     */
    public interface ImagePickerListener {
        void onCamera();

        void onGallery();

        void onDelete();
    }
}
