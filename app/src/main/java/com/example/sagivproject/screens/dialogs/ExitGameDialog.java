package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import com.example.sagivproject.R;

public class ExitGameDialog {
    private final Context context;
    private final Runnable onConfirm;

    public ExitGameDialog(Context context, Runnable onConfirm) {
        this.context = context;
        this.onConfirm = onConfirm;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_exit);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        TextView txtTitle = dialog.findViewById(R.id.txt_DialogExit_title);
        TextView txtMessage = dialog.findViewById(R.id.txt_DialogExit_message);
        Button btnConfirm = dialog.findViewById(R.id.btn_DialogExit_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_DialogExit_cancel);

        txtTitle.setText("יציאה מהמשחק");
        txtMessage.setText("האם ברצונך לצאת מהמשחק?");

        btnConfirm.setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
