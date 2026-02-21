package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import com.example.sagivproject.R;

import java.util.Objects;

/**
 * A confirmation dialog for logging out of the application.
 * <p>
 * This dialog asks the user to confirm if they want to log out. If they confirm,
 * a provided {@link Runnable} is executed, which handles the actual logout logic.
 * </p>
 */
public class LogoutDialog {
    private final Context context;
    private final Runnable onConfirm;

    /**
     * Constructs a new LogoutDialog.
     *
     * @param context   The context in which the dialog should be shown.
     * @param onConfirm The runnable to be executed if the user confirms the logout.
     */
    public LogoutDialog(Context context, Runnable onConfirm) {
        this.context = context;
        this.onConfirm = onConfirm;
    }

    /**
     * Creates and displays the dialog.
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

        txtTitle.setText("התנתקות");
        txtMessage.setText("האם ברצונך להתנתק?");

        btnConfirm.setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
