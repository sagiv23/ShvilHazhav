package com.example.sagivproject.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.sagivproject.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment that presents options for changing or deleting a user's profile image.
 * <p>
 * This dialog provides buttons to:
 * <ul>
 * <li>Launch the camera to take a new photo.</li>
 * <li>Open the system gallery to select an existing image.</li>
 * <li>Delete the current profile image (only visible if an image exists).</li>
 * </ul>
 * Interaction results are passed back to the caller via the {@link ImagePickerListener}.
 * </p>
 */
@AndroidEntryPoint
public class ProfileImageDialog extends BaseDialog {
    private static final String ARG_HAS_IMAGE = "arg_has_image";

    private boolean hasImage;
    private ImagePickerListener listener;

    /**
     * Default constructor for Hilt.
     */
    @Inject
    public ProfileImageDialog() {
    }

    /**
     * Configures the dialog state.
     *
     * @param hasImage true if the user currently has a profile picture set.
     * @param listener Callback for image selection actions.
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

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_profile_image;
    }

    @Override
    protected void setupViews(Dialog dialog) {
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
    }

    /**
     * Interface for handling profile image picker interactions.
     */
    public interface ImagePickerListener {
        /**
         * Called when the 'Camera' option is selected.
         */
        void onCamera();

        /**
         * Called when the 'Gallery' option is selected.
         */
        void onGallery();

        /**
         * Called when the 'Delete' option is selected.
         */
        void onDelete();
    }
}