package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment for displaying an image in full-screen mode.
 * <p>
 * This dialog is styled to use the entire screen and provides a simple way to
 * inspect images (like profile pictures or medication cards) in detail.
 * Tapping the image dismisses the dialog.
 * </p>
 */
@AndroidEntryPoint
public class FullImageDialog extends DialogFragment {
    private Drawable imageDrawable;

    /**
     * Constructs a new FullImageDialog.
     */
    @Inject
    public FullImageDialog() {
    }

    /**
     * Sets the image drawable to be displayed in full screen.
     *
     * @param imageDrawable The drawable to show.
     */
    public void setImage(Drawable imageDrawable) {
        this.imageDrawable = imageDrawable;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set full-screen theme
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.dialog_full_image);
        dialog.setCancelable(true);

        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
        if (imageDrawable != null) {
            dialogImage.setImageDrawable(imageDrawable);
        }

        // Tap anywhere on the image to close
        dialogImage.setOnClickListener(v -> dismiss());

        return dialog;
    }
}
