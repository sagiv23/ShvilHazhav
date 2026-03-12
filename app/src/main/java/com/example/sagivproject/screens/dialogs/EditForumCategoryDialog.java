package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumCategory;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog for editing the name of a forum category, implemented as a DialogFragment.
 */
@AndroidEntryPoint
public class EditForumCategoryDialog extends DialogFragment {
    private ForumCategory category;
    private EditForumCategoryDialogListener listener;

    @Inject
    public EditForumCategoryDialog() {
    }

    public void setData(ForumCategory category, EditForumCategoryDialogListener listener) {
        this.category = category;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_edit_forum_category);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        if (category == null) return dialog;

        EditText inputCategoryName = dialog.findViewById(R.id.inputEditCategoryName);
        Button btnSave = dialog.findViewById(R.id.btnEditCategorySave);
        Button btnCancel = dialog.findViewById(R.id.btnEditCategoryCancel);

        inputCategoryName.setText(category.getName());

        btnSave.setOnClickListener(v -> {
            String newName = inputCategoryName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "שם הקטגוריה לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onUpdateCategory(newName);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return dialog;
    }

    public interface EditForumCategoryDialogListener {
        void onUpdateCategory(String newName);
    }
}
