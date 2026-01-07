package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.example.sagivproject.R;

public class FullImageDialog {
    private final Context context;
    private final Drawable imageDrawable;

    public FullImageDialog(Context context, Drawable imageDrawable) {
        this.context = context;
        this.imageDrawable = imageDrawable;
    }

    public void show() {
        Dialog dialog = new Dialog(
                context,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen
        );

        dialog.setContentView(R.layout.dialog_full_image);
        dialog.setCancelable(true);

        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);

        //הצבת התמונה
        dialogImage.setImageDrawable(imageDrawable);

        //לחיצה על התמונה - סגירה
        dialogImage.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}