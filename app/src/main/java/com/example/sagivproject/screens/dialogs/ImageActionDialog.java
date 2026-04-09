package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A unified dialog for displaying an image and performing actions (Camera, Gallery, Delete).
 */
@AndroidEntryPoint
public class ImageActionDialog extends BaseDialog {
    private static final String ARG_HAS_ACTIONS = "arg_has_actions";
    private static final String ARG_HAS_DELETE = "arg_has_delete";

    private Drawable imageDrawable;
    private ImageActionListener listener;

    @Inject
    public ImageActionDialog() {
    }

    public void setData(Drawable image, boolean showActions, boolean showDelete, ImageActionListener listener) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_HAS_ACTIONS, showActions);
        args.putBoolean(ARG_HAS_DELETE, showDelete);
        setArguments(args);
        this.imageDrawable = image;
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If we only show the image, make it full screen style
        if (getArguments() != null && !getArguments().getBoolean(ARG_HAS_ACTIONS)) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.dialog_image_action;
    }

    @Override
    protected void setupView(Dialog dialog) {
        Bundle args = getArguments();
        boolean showActions = args != null && args.getBoolean(ARG_HAS_ACTIONS);
        boolean showDelete = args != null && args.getBoolean(ARG_HAS_DELETE);

        ImageView imageView = dialog.findViewById(R.id.img_dialog_full);
        View layoutActions = dialog.findViewById(R.id.layout_image_actions);
        View btnCamera = dialog.findViewById(R.id.btn_image_camera);
        View btnGallery = dialog.findViewById(R.id.btn_image_gallery);
        View btnDelete = dialog.findViewById(R.id.btn_image_delete);
        View btnClose = dialog.findViewById(R.id.btn_image_close);

        if (imageDrawable != null) {
            imageView.setImageDrawable(imageDrawable);
        }

        if (showActions) {
            layoutActions.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(showDelete ? View.VISIBLE : View.GONE);
        } else {
            layoutActions.setVisibility(View.GONE);
            // In full screen mode, clicking the image closes it
            imageView.setOnClickListener(v -> dismiss());
        }

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

        btnClose.setOnClickListener(v -> dismiss());
    }

    public interface ImageActionListener {
        void onCamera();

        void onGallery();

        void onDelete();
    }
}
