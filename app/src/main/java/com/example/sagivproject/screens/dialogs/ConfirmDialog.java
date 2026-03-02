package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sagivproject.R;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A generic confirmation dialog that can be reused for various scenarios.
 * <p>
 * This dialog can be configured with a custom title, message, and button texts.
 * It supports both a single action (confirm only) and a dual action (confirm/cancel).
 * </p>
 */
@ActivityScoped
public class ConfirmDialog {
    private final Context context;

    /**
     * Constructs a new ConfirmDialog.
     * Hilt uses this constructor to provide an instance.
     *
     * @param context The context in which the dialog should be shown.
     */
    @Inject
    public ConfirmDialog(@ActivityContext Context context) {
        this.context = context;
    }

    /**
     * Shows a dual-action dialog (e.g., "OK" and "Cancel").
     */
    public void show(String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        createAndShow(title, message, confirmText, cancelText, onConfirm, false);
    }

    /**
     * Shows a single-action dialog (e.g., an alert with just an "OK" button).
     */
    public void show(String title, String message, Runnable onConfirm) {
        createAndShow(title, message, "אישור", null, onConfirm, true);
    }

    private void createAndShow(String title, String message, String confirmText, String cancelText, Runnable onConfirm, boolean isSingleAction) {
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
            btnCancel.setVisibility(View.VISIBLE);
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
