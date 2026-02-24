package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sagivproject.R;

import java.util.Objects;

/**
 * A generic confirmation dialog that can be reused for various scenarios.
 * <p>
 * This dialog can be configured with a custom title, message, and button texts.
 * It supports both a single action (confirm only) and a dual action (confirm/cancel).
 * </p>
 */
public class ConfirmDialog {
    private final Context context;
    private final String title;
    private final String message;
    private final String confirmText;
    private final String cancelText;
    private final Runnable onConfirm;
    private final boolean isSingleAction;

    /**
     * Constructor for a dual-action dialog (e.g., "OK" and "Cancel").
     */
    public ConfirmDialog(Context context, String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.confirmText = confirmText;
        this.cancelText = cancelText;
        this.onConfirm = onConfirm;
        this.isSingleAction = false;
    }

    /**
     * Constructor for a single-action dialog (e.g., an alert with just an "OK" button).
     */
    public ConfirmDialog(Context context, String title, String message, Runnable onConfirm) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.confirmText = "OK"; // Default confirm text
        this.cancelText = null; // No cancel button
        this.onConfirm = onConfirm;
        this.isSingleAction = true;
    }

    /**
     * Creates and displays the configured dialog.
     */
    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        TextView txtTitle = dialog.findViewById(R.id.txt_DialogConfirm_title);
        TextView txtMessage = dialog.findViewById(R.id.txt_DialogConfirm_message);
        Button btnConfirm = dialog.findViewById(R.id.btn_DialogConfirm_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_DialogConfirm_cancel);

        txtTitle.setText(title);
        txtMessage.setText(message);
        btnConfirm.setText(confirmText);

        if (isSingleAction) {
            btnCancel.setVisibility(View.GONE);
        } else {
            btnCancel.setText(cancelText);
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        btnConfirm.setOnClickListener(v -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
