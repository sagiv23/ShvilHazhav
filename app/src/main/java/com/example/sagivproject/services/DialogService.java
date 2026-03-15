package com.example.sagivproject.services;

import android.graphics.drawable.Drawable;

import androidx.fragment.app.FragmentManager;

import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.AddEmergencyContactDialog;
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
 * <p>
 * This class uses Hilt {@link Provider}s to lazily instantiate {@link androidx.fragment.app.DialogFragment}s
 * when they need to be shown. It provides a clean API for fragments to trigger complex UI dialogs
 * without needing to know the implementation details or manage the FragmentManager directly for every call.
 * </p>
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
    private final Provider<AddEmergencyContactDialog> addEmergencyContactDialogProvider;

    /**
     * Constructs a new DialogService with providers for various dialog fragments.
     */
    @Inject
    public DialogService(
            Provider<MedicationDialog> medicationDialogProvider,
            Provider<AddUserDialog> addUserDialogProvider,
            Provider<EditUserDialog> editUserDialogProvider,
            Provider<EditForumCategoryDialog> editForumCategoryDialogProvider,
            Provider<FullImageDialog> fullImageDialogProvider,
            Provider<ProfileImageDialog> profileImageDialogProvider,
            Provider<ConfirmDialog> confirmDialogProvider,
            Provider<AddEmergencyContactDialog> addEmergencyContactDialogProvider
    ) {
        this.medicationDialogProvider = medicationDialogProvider;
        this.addUserDialogProvider = addUserDialogProvider;
        this.editUserDialogProvider = editUserDialogProvider;
        this.editForumCategoryDialogProvider = editForumCategoryDialogProvider;
        this.fullImageDialogProvider = fullImageDialogProvider;
        this.profileImageDialogProvider = profileImageDialogProvider;
        this.confirmDialogProvider = confirmDialogProvider;
        this.addEmergencyContactDialogProvider = addEmergencyContactDialogProvider;
    }

    /**
     * Shows a dialog to add or edit a medication.
     *
     * @param fm        The {@link FragmentManager}.
     * @param medToEdit The medication to edit, or null to add a new one.
     * @param listener  The listener for submit actions.
     */
    public void showMedicationDialog(FragmentManager fm, Medication medToEdit, MedicationDialog.OnMedicationSubmitListener listener) {
        MedicationDialog dialog = medicationDialogProvider.get();
        dialog.setData(medToEdit, listener);
        dialog.show(fm, "MedicationDialog");
    }

    /**
     * Shows a dialog for an administrator to add a new user.
     *
     * @param fm       The {@link FragmentManager}.
     * @param listener The listener for user addition.
     */
    public void showAddUserDialog(FragmentManager fm, AddUserDialog.AddUserDialogListener listener) {
        AddUserDialog dialog = addUserDialogProvider.get();
        dialog.setListener(listener);
        dialog.show(fm, "AddUserDialog");
    }

    /**
     * Shows a dialog to edit an existing user's profile.
     *
     * @param fm       The {@link FragmentManager}.
     * @param user     The user to edit.
     * @param listener The listener for update actions.
     */
    public void showEditUserDialog(FragmentManager fm, User user, EditUserDialog.EditUserDialogListener listener) {
        EditUserDialog dialog = editUserDialogProvider.get();
        dialog.setData(user, listener);
        dialog.show(fm, "EditUserDialog");
    }

    /**
     * Shows a dialog to edit a forum category name.
     *
     * @param fm       The {@link FragmentManager}.
     * @param category The category to edit.
     * @param listener The listener for name update actions.
     */
    public void showEditForumCategoryDialog(FragmentManager fm, ForumCategory category, EditForumCategoryDialog.EditForumCategoryDialogListener listener) {
        EditForumCategoryDialog dialog = editForumCategoryDialogProvider.get();
        dialog.setData(category, listener);
        dialog.show(fm, "EditForumCategoryDialog");
    }

    /**
     * Shows a dialog displaying a full-screen image.
     *
     * @param fm            The {@link FragmentManager}.
     * @param imageDrawable The drawable to display.
     */
    public void showFullImageDialog(FragmentManager fm, Drawable imageDrawable) {
        FullImageDialog dialog = fullImageDialogProvider.get();
        dialog.setImage(imageDrawable);
        dialog.show(fm, "FullImageDialog");
    }

    /**
     * Shows a dialog to pick or delete a profile image.
     *
     * @param fm       The {@link FragmentManager}.
     * @param hasImage Whether the user already has a profile image.
     * @param listener The listener for image picking actions.
     */
    public void showProfileImageDialog(FragmentManager fm, boolean hasImage, ProfileImageDialog.ImagePickerListener listener) {
        ProfileImageDialog dialog = profileImageDialogProvider.get();
        dialog.setData(hasImage, listener);
        dialog.show(fm, "ProfileImageDialog");
    }

    /**
     * Shows a dialog to add or edit an emergency contact.
     *
     * @param fm       The {@link FragmentManager}.
     * @param contact  The contact to edit, or null to add a new one.
     * @param listener The listener for contact actions.
     */
    public void showEmergencyContactDialog(FragmentManager fm, EmergencyContact contact, AddEmergencyContactDialog.AddEmergencyContactListener listener) {
        AddEmergencyContactDialog dialog = addEmergencyContactDialogProvider.get();
        dialog.setData(contact, listener);
        dialog.show(fm, "AddEmergencyContactDialog");
    }

    /**
     * Shows a confirmation or alert dialog.
     * <p>
     * If cancelText is null or empty, it acts as an alert (single button).
     * </p>
     *
     * @param fm          The {@link FragmentManager}.
     * @param title       The dialog title.
     * @param message     The dialog message.
     * @param confirmText The text for the confirm button.
     * @param cancelText  The text for the cancel button (optional).
     * @param onConfirm   The action to perform on confirmation.
     */
    public void showConfirmDialog(FragmentManager fm, String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        ConfirmDialog dialog = confirmDialogProvider.get();
        dialog.setData(title, message, confirmText, cancelText, onConfirm);
        dialog.show(fm, "ConfirmDialog");
    }
}
