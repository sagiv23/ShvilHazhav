package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.example.sagivproject.R;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A dialog for displaying an image in full-screen mode.
 * <p>
 * This dialog takes a {@link Drawable} and displays it in a full-screen, immersive view.
 * The dialog can be dismissed by tapping on the image.
 * </p>
 */
@ActivityScoped
public class FullImageDialog {
    private final Context context;

    /**
     * Constructs a new FullImageDialog.
     * Hilt uses this constructor to provide an instance.
     *
     * @param context The context in which the dialog should be shown.
     */
    @Inject
    public FullImageDialog(@ActivityContext Context context) {
        this.context = context;
    }

    /**
     * Creates and displays the full-screen image dialog.
     *
     * @param imageDrawable The drawable resource of the image to be displayed.
     */
    public void show(Drawable imageDrawable) {
        Dialog dialog = new Dialog(
                context,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen
        );

        dialog.setContentView(R.layout.dialog_full_image);
        dialog.setCancelable(true);

        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);

        // Set the image drawable
        dialogImage.setImageDrawable(imageDrawable);

        // Set a click listener on the image to dismiss the dialog
        dialogImage.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Factory interface for creating FullImageDialog instances (if needed for assisted injection).
     * In this project, we use simple method-based injection.
     */
    public interface Factory {
        FullImageDialog create(Drawable imageDrawable);
    }
}
