package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseDialog;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment for displaying an image in an immersive full-screen mode.
 * <p>
 * This dialog is styled to remove system bars and use the entire screen real estate.
 * It is used for inspecting profile pictures or medication card images in detail.
 * Tapping anywhere on the image dismisses the dialog.
 * </p>
 */
@AndroidEntryPoint
public class FullImageDialog extends BaseDialog {
    /**
     * The image content to display.
     */
    private Drawable imageDrawable;

    /**
     * Constructs a new FullImageDialog.
     */
    @Inject
    public FullImageDialog() {
    }

    /**
     * Sets the image drawable to be displayed.
     *
     * @param imageDrawable The {@link Drawable} source.
     */
    public void setImage(Drawable imageDrawable) {
        this.imageDrawable = imageDrawable;
    }

    /**
     * Configures the dialog style to be full-screen before creation.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    protected boolean shouldSetTransparentBackground() {
        return false;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_full_image;
    }

    @Override
    protected void setupViews(Dialog dialog) {
        dialog.setCancelable(true);

        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
        if (imageDrawable != null) {
            dialogImage.setImageDrawable(imageDrawable);
        }

        dialogImage.setOnClickListener(v -> dismiss());
    }
}