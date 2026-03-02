package com.example.sagivproject.services;

import android.graphics.drawable.Drawable;

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

import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A service that manages the creation and display of all dialogs in the application.
 * <p>
 * This class uses Hilt to inject and manage the lifecycle of various dialog handlers.
 * By centralizing dialog access, it ensures consistent behavior and simplifies
 * dependency management in Activities.
 * </p>
 */
@ActivityScoped
public class DialogService {
    private final MedicationDialog medicationDialog;
    private final AddUserDialog addUserDialog;
    private final EditUserDialog editUserDialog;
    private final EditForumCategoryDialog editForumCategoryDialog;
    private final FullImageDialog fullImageDialog;
    private final ProfileImageDialog profileImageDialog;
    private final ConfirmDialog confirmDialog;

    @Inject
    public DialogService(
            MedicationDialog medicationDialog,
            AddUserDialog addUserDialog,
            EditUserDialog editUserDialog,
            EditForumCategoryDialog editForumCategoryDialog,
            FullImageDialog fullImageDialog,
            ProfileImageDialog profileImageDialog,
            ConfirmDialog confirmDialog
    ) {
        this.medicationDialog = medicationDialog;
        this.addUserDialog = addUserDialog;
        this.editUserDialog = editUserDialog;
        this.editForumCategoryDialog = editForumCategoryDialog;
        this.fullImageDialog = fullImageDialog;
        this.profileImageDialog = profileImageDialog;
        this.confirmDialog = confirmDialog;
    }

    public void showMedicationDialog(Medication medToEdit, MedicationDialog.OnMedicationSubmitListener listener) {
        medicationDialog.show(medToEdit, listener);
    }

    public void showAddUserDialog(AddUserDialog.AddUserDialogListener listener) {
        addUserDialog.show(listener);
    }

    public void showEditUserDialog(User user, EditUserDialog.EditUserDialogListener listener) {
        editUserDialog.show(user, listener);
    }

    public void showEditForumCategoryDialog(ForumCategory category, EditForumCategoryDialog.EditForumCategoryDialogListener listener) {
        editForumCategoryDialog.show(category, listener);
    }

    public void showFullImageDialog(Drawable imageDrawable) {
        fullImageDialog.show(imageDrawable);
    }

    public void showProfileImageDialog(boolean hasImage, ProfileImageDialog.ImagePickerListener listener) {
        profileImageDialog.show(hasImage, listener);
    }

    public void showConfirmDialog(String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        confirmDialog.show(title, message, confirmText, cancelText, onConfirm);
    }

    public void showConfirmDialog(String title, String message, Runnable onConfirm) {
        confirmDialog.show(title, message, onConfirm);
    }
}
