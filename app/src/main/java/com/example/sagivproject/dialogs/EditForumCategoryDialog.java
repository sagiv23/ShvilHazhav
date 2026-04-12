package com.example.sagivproject.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumCategory;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment for editing the name of an existing forum category.
 * <p>
 * This dialog is used by administrators to rename categories. It ensures the name is not empty
 * and communicates the change back via the {@link EditForumCategoryDialogListener}.
 * It leverages {@link BundleCompat} for safe argument retrieval.
 * </p>
 */
@AndroidEntryPoint
public class EditForumCategoryDialog extends BaseDialog {
    /**
     * Argument key for passing the category object.
     */
    private static final String ARG_CATEGORY = "arg_category";

    /**
     * The category being edited.
     */
    private ForumCategory category;

    /**
     * Listener for returning the updated category name.
     */
    private EditForumCategoryDialogListener listener;

    /**
     * Default constructor for Hilt.
     */
    @Inject
    public EditForumCategoryDialog() {
    }

    /**
     * Configures the dialog with the category to edit and the submission listener.
     *
     * @param category The {@link ForumCategory} to edit.
     * @param listener The listener to handle the update action.
     */
    public void setData(ForumCategory category, EditForumCategoryDialogListener listener) {
        Bundle args = new Bundle();
        if (category != null) {
            args.putSerializable(ARG_CATEGORY, category);
        }
        setArguments(args);
        this.listener = listener;
    }

    /**
     * Retrieves the category object from the arguments bundle.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = BundleCompat.getSerializable(getArguments(), ARG_CATEGORY, ForumCategory.class);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_edit_forum_category;
    }

    @Override
    protected void setupViews(Dialog dialog) {
        if (category == null) return;

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
    }

    /**
     * Interface for listening to forum category update events.
     */
    public interface EditForumCategoryDialogListener {
        /**
         * Called when the administrator submits a valid new name for the category.
         *
         * @param newName The updated display name.
         */
        void onUpdateCategory(String newName);
    }
}