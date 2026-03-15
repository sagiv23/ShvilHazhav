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
 * A dialog fragment that presents options for changing or deleting a user's profile image.
 * <p>
 * This dialog provides buttons to trigger the camera, open the gallery, or delete the
 * current profile image (if one exists). Interaction events are communicated back
 * via the {@link ImagePickerListener}.
 * </p>
 */
@AndroidEntryPoint
public class ProfileImageDialog extends DialogFragment {
    private static final String ARG_HAS_IMAGE = "arg_has_image";

    private boolean hasImage;
    private ImagePickerListener listener;

    /**
     * Constructs a new ProfileImageDialog.
     */
    @Inject
    public ProfileImageDialog() {
    }

    /**
     * Sets the data for the dialog and the picker listener.
     *
     * @param hasImage Whether the user currently has a profile image set.
     * @param listener The listener to handle image source selection or deletion.
     */
    public void setData(boolean hasImage, ImagePickerListener listener) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_HAS_IMAGE, hasImage);
        setArguments(args);
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hasImage = getArguments().getBoolean(ARG_HAS_IMAGE);
        }
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

        // Only show the delete option if an image is already present
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

    /**
     * Listener interface for handling profile image selection actions.
     */
    public interface ImagePickerListener {
        /**
         * Called when the camera option is selected.
         */
        void onCamera();

        /**
         * Called when the gallery option is selected.
         */
        void onGallery();

        /**
         * Called when the delete option is selected.
         */
        void onDelete();
    }
}
