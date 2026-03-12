package com.example.sagivproject.services;

import android.graphics.drawable.Drawable;

import androidx.fragment.app.FragmentManager;

import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.AddUserDialog;
import com.example.sagivproject.screens.dialogs.ConfirmDialog;
import com.example.sagivproject.screens.dialogs.EditForumCategoryDialog;
import com.example.sagivproject.screens.dialogs.EditUserDialog;
import com.example.sagivproject.screens.dialogs.FullImageDialog;
import com.example.sagivproject.screens.dialogs.MedicationDialog;
import com.example.sagivproject.screens.dialogs.ProfileImageDialog;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A service that manages the creation and display of all dialogs in the application.
 * Updated to support DialogFragments and Hilt injection.
 */
@ActivityScoped
public class DialogService {

    private final Provider<MedicationDialog> medicationDialogProvider;
    private final Provider<AddUserDialog> addUserDialogProvider;
    private final Provider<EditUserDialog> editUserDialogProvider;
    private final Provider<EditForumCategoryDialog> editForumCategoryDialogProvider;
    private final Provider<FullImageDialog> fullImageDialogProvider;
    private final Provider<ProfileImageDialog> profileImageDialogProvider;
    private final Provider<ConfirmDialog> confirmDialogProvider;

    @Inject
    public DialogService(
            Provider<MedicationDialog> medicationDialogProvider,
            Provider<AddUserDialog> addUserDialogProvider,
            Provider<EditUserDialog> editUserDialogProvider,
            Provider<EditForumCategoryDialog> editForumCategoryDialogProvider,
            Provider<FullImageDialog> fullImageDialogProvider,
            Provider<ProfileImageDialog> profileImageDialogProvider,
            Provider<ConfirmDialog> confirmDialogProvider
    ) {
        this.medicationDialogProvider = medicationDialogProvider;
        this.addUserDialogProvider = addUserDialogProvider;
        this.editUserDialogProvider = editUserDialogProvider;
        this.editForumCategoryDialogProvider = editForumCategoryDialogProvider;
        this.fullImageDialogProvider = fullImageDialogProvider;
        this.profileImageDialogProvider = profileImageDialogProvider;
        this.confirmDialogProvider = confirmDialogProvider;
    }

    public void showMedicationDialog(FragmentManager fm, Medication medToEdit, MedicationDialog.OnMedicationSubmitListener listener) {
        MedicationDialog dialog = medicationDialogProvider.get();
        dialog.setData(medToEdit, listener);
        dialog.show(fm, "MedicationDialog");
    }

    public void showAddUserDialog(FragmentManager fm, AddUserDialog.AddUserDialogListener listener) {
        AddUserDialog dialog = addUserDialogProvider.get();
        dialog.setListener(listener);
        dialog.show(fm, "AddUserDialog");
    }

    public void showEditUserDialog(FragmentManager fm, User user, EditUserDialog.EditUserDialogListener listener) {
        EditUserDialog dialog = editUserDialogProvider.get();
        dialog.setData(user, listener);
        dialog.show(fm, "EditUserDialog");
    }

    public void showEditForumCategoryDialog(FragmentManager fm, ForumCategory category, EditForumCategoryDialog.EditForumCategoryDialogListener listener) {
        EditForumCategoryDialog dialog = editForumCategoryDialogProvider.get();
        dialog.setData(category, listener);
        dialog.show(fm, "EditForumCategoryDialog");
    }

    public void showFullImageDialog(FragmentManager fm, Drawable imageDrawable) {
        FullImageDialog dialog = fullImageDialogProvider.get();
        dialog.setImage(imageDrawable);
        dialog.show(fm, "FullImageDialog");
    }

    public void showProfileImageDialog(FragmentManager fm, boolean hasImage, ProfileImageDialog.ImagePickerListener listener) {
        ProfileImageDialog dialog = profileImageDialogProvider.get();
        dialog.setData(hasImage, listener);
        dialog.show(fm, "ProfileImageDialog");
    }

    /**
     * Shows a confirmation or alert dialog.
     * If cancelText is null or empty, it acts as an alert (single button).
     */
    public void showConfirmDialog(FragmentManager fm, String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        ConfirmDialog dialog = confirmDialogProvider.get();
        dialog.setData(title, message, confirmText, cancelText, onConfirm);
        dialog.show(fm, "ConfirmDialog");
    }
}
