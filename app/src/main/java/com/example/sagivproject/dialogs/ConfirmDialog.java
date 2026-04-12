package com.example.sagivproject.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sagivproject.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A generic, reusable confirmation dialog fragment.
 * <p>
 * This dialog provides a standardized layout for showing messages to the user with either
 * a single "OK" button (acting as an alert) or both "Confirm" and "Cancel" buttons.
 * It uses a functional {@link Runnable} to execute the confirmed action.
 * </p>
 */
@AndroidEntryPoint
public class ConfirmDialog extends BaseDialog {
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_CONFIRM_TEXT = "confirm_text";
    private static final String ARG_CANCEL_TEXT = "cancel_text";

    /**
     * The logic to execute when the user clicks the positive button.
     */
    private Runnable onConfirm;

    /**
     * Constructs a new ConfirmDialog.
     */
    @Inject
    public ConfirmDialog() {
    }

    /**
     * Configures the dialog with specific content and behavior.
     *
     * @param title       The text to display in the dialog title.
     * @param message     The descriptive body text.
     * @param confirmText The label for the positive button (defaults to "אישור").
     * @param cancelText  The label for the negative button (if null, the button is hidden).
     * @param onConfirm   The Runnable to run upon confirmation.
     */
    public void setData(String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_CONFIRM_TEXT, confirmText != null ? confirmText : "אישור");
        args.putString(ARG_CANCEL_TEXT, cancelText);
        setArguments(args);
        this.onConfirm = onConfirm;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_confirm;
    }

    @Override
    protected void setupViews(Dialog dialog) {
        Bundle args = getArguments();
        if (args == null) return;

        TextView txtTitle = dialog.findViewById(R.id.txt_DialogConfirm_title);
        TextView txtMessage = dialog.findViewById(R.id.txt_DialogConfirm_message);
        Button btnConfirm = dialog.findViewById(R.id.btn_DialogConfirm_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_DialogConfirm_cancel);

        txtTitle.setText(args.getString(ARG_TITLE));
        txtMessage.setText(args.getString(ARG_MESSAGE));
        btnConfirm.setText(args.getString(ARG_CONFIRM_TEXT));

        String cancelText = args.getString(ARG_CANCEL_TEXT);
        if (cancelText == null || cancelText.isEmpty()) {
            btnCancel.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText(cancelText);
            btnCancel.setOnClickListener(v -> dismiss());
        }

        btnConfirm.setOnClickListener(v -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
            dismiss();
        });
    }
}