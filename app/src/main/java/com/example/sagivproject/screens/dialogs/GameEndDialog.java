package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import com.example.sagivproject.R;

import java.util.Objects;

/**
 * A dialog to announce the end of a memory game.
 * <p>
 * This dialog displays a message indicating the outcome of the game (win, loss, or draw)
 * and provides a single action button to return to the main game menu.
 * </p>
 */
public class GameEndDialog {
    private final Context context;
    private final String message;
    private final Runnable onExitToMenu;

    /**
     * Constructs a new GameEndDialog.
     *
     * @param context      The context in which the dialog should be shown.
     * @param message      The message to display (e.g., "You won!", "You lost.").
     * @param onExitToMenu The runnable to be executed when the user clicks the exit button.
     */
    public GameEndDialog(Context context, String message, Runnable onExitToMenu) {
        this.context = context;
        this.message = message;
        this.onExitToMenu = onExitToMenu;
    }

    /**
     * Creates and displays the dialog.
     */
    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        TextView txtTitle = dialog.findViewById(R.id.txt_DialogConfirm_title);
        TextView txtMessage = dialog.findViewById(R.id.txt_DialogConfirm_message);
        Button btnConfirm = dialog.findViewById(R.id.btn_DialogConfirm_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_DialogConfirm_cancel);

        txtTitle.setText("המשחק הסתיים");
        txtMessage.setText(message);

        // Configure for a single action
        btnConfirm.setText("חזור לתפריט");
        btnCancel.setVisibility(Button.GONE);

        btnConfirm.setOnClickListener(v -> {
            onExitToMenu.run();
            dialog.dismiss();
        });

        dialog.show();
    }
}
