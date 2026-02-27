package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumCategory;

import java.util.Objects;

public class EditForumCategoryDialog {
    private final Context context;
    private final ForumCategory category;
    private final EditForumCategoryDialogListener listener;

    public EditForumCategoryDialog(Context context, ForumCategory category, EditForumCategoryDialogListener listener) {
        this.context = context;
        this.category = category;
        this.listener = listener;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_forum_category);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        EditText inputCategoryName = dialog.findViewById(R.id.inputEditCategoryName);
        Button btnSave = dialog.findViewById(R.id.btnEditCategorySave);
        Button btnCancel = dialog.findViewById(R.id.btnEditCategoryCancel);

        inputCategoryName.setText(category.getName());

        btnSave.setOnClickListener(v -> {
            String newName = inputCategoryName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(context, "שם הקטגוריה לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onUpdateCategory(newName);
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public interface EditForumCategoryDialogListener {
        void onUpdateCategory(String newName);
    }
}
