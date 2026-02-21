package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.example.sagivproject.R;

import java.util.Objects;

/**
 * A dialog that presents options for changing a user's profile image.
 * <p>
 * This dialog provides buttons to select a new image from the camera or gallery.
 * It also provides an option to delete the current profile image, if one exists.
 * </p>
 */
public class ProfileImageDialog {
    private final Context context;
    private final boolean hasImage;
    private final ImagePickerListener listener;

    /**
     * Constructs a new ProfileImageDialog.
     *
     * @param context  The context in which the dialog should be shown.
     * @param hasImage True if the user currently has a profile image, which determines if the delete option is shown.
     * @param listener The listener to be invoked when an option is selected.
     */
    public ProfileImageDialog(Context context, boolean hasImage, ImagePickerListener listener) {
        this.context = context;
        this.hasImage = hasImage;
        this.listener = listener;
    }

    /**
     * Creates and displays the dialog.
     */
    public void show() {
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

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * An interface for listeners that are invoked when an image source option is selected.
     */
    public interface ImagePickerListener {
        /**
         * Called when the "Camera" option is selected.
         */
        void onCamera();

        /**
         * Called when the "Gallery" option is selected.
         */
        void onGallery();

        /**
         * Called when the "Delete" option is selected.
         */
        void onDelete();
    }
}
