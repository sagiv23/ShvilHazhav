package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumCategory;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A dialog for editing the name of a forum category.
 */
@ActivityScoped
public class EditForumCategoryDialog {
    private final Context context;

    /**
     * Constructs a new EditForumCategoryDialog.
     * Hilt uses this constructor to provide an instance.
     *
     * @param context The context in which the dialog should be shown.
     */
    @Inject
    public EditForumCategoryDialog(@ActivityContext Context context) {
        this.context = context;
    }

    /**
     * Creates and displays the dialog.
     *
     * @param category The category to be edited.
     * @param listener The listener for the update action.
     */
    public void show(ForumCategory category, EditForumCategoryDialogListener listener) {
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

    /**
     * An interface to listen for the update category action.
     */
    public interface EditForumCategoryDialogListener {
        void onUpdateCategory(String newName);
    }
}
