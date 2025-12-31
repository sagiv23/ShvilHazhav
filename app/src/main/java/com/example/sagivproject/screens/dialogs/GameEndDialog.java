package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import com.example.sagivproject.R;

public class GameEndDialog {
    private final Context context;
    private final String message;
    private final Runnable onExitToMenu;

    public GameEndDialog(Context context, String message, Runnable onExitToMenu) {
        this.context = context;
        this.message = message;
        this.onExitToMenu = onExitToMenu;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_exit);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        TextView txtTitle = dialog.findViewById(R.id.txt_DialogExit_title);
        TextView txtMessage = dialog.findViewById(R.id.txt_DialogExit_message);
        Button btnConfirm = dialog.findViewById(R.id.btn_DialogExit_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_DialogExit_cancel);

        txtTitle.setText("המשחק הסתיים");
        txtMessage.setText(message);

        btnConfirm.setText("חזור לתפריט");
        btnCancel.setVisibility(Button.GONE);

        btnConfirm.setOnClickListener(v -> {
            onExitToMenu.run();
            dialog.dismiss();
        });

        dialog.show();
    }
}