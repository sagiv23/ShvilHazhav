package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.example.sagivproject.R;

/**
 * A dialog for displaying an image in full-screen mode.
 * <p>
 * This dialog takes a {@link Drawable} and displays it in a full-screen, immersive view.
 * The dialog can be dismissed by tapping on the image.
 * </p>
 */
public class FullImageDialog {
    private final Context context;
    private final Drawable imageDrawable;

    /**
     * Constructs a new FullImageDialog.
     *
     * @param context       The context in which the dialog should be shown.
     * @param imageDrawable The drawable resource of the image to be displayed.
     */
    public FullImageDialog(Context context, Drawable imageDrawable) {
        this.context = context;
        this.imageDrawable = imageDrawable;
    }

    /**
     * Creates and displays the full-screen image dialog.
     */
    public void show() {
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
}
