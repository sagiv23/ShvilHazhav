package com.example.sagivproject.services.impl;

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
import com.example.sagivproject.services.IDialogService;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A service that manages the creation and display of all dialog fragments in the application.
 * <p>
 * This class uses Hilt {@link Provider}s to lazily instantiate {@link androidx.fragment.app.DialogFragment}s
 * only when they are requested. It provides a clean, unified API for activities and fragments
 * to trigger complex UI dialogs without managing FragmentManager transactions directly.
 * </p>
 */
@ActivityScoped
public class DialogService implements IDialogService {
    private final Provider<MedicationDialog> medicationDialogProvider;
    private final Provider<AddUserDialog> addUserDialogProvider;
    private final Provider<EditUserDialog> editUserDialogProvider;
    private final Provider<EditForumCategoryDialog> editForumCategoryDialogProvider;
    private final Provider<FullImageDialog> fullImageDialogProvider;
    private final Provider<ProfileImageDialog> profileImageDialogProvider;
    private final Provider<ConfirmDialog> confirmDialogProvider;
    private final Provider<AddEmergencyContactDialog> addEmergencyContactDialogProvider;

    /**
     * Constructs a new DialogService with providers for all dialog fragments.
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
     * Displays a dialog to add a new medication or edit an existing one.
     *
     * @param fm        The {@link FragmentManager} used to show the dialog.
     * @param medToEdit The {@link Medication} object to edit, or null to add a new one.
     * @param listener  The listener to handle submission events.
     */
    @Override
    public void showMedicationDialog(FragmentManager fm, Medication medToEdit, MedicationDialog.OnMedicationSubmitListener listener) {
        MedicationDialog dialog = medicationDialogProvider.get();
        dialog.setData(medToEdit, listener);
        dialog.show(fm, "MedicationDialog");
    }

    /**
     * Displays a dialog for administrators to create a new user profile.
     *
     * @param fm       The {@link FragmentManager}.
     * @param listener The listener to handle the user creation logic.
     */
    @Override
    public void showAddUserDialog(FragmentManager fm, AddUserDialog.AddUserDialogListener listener) {
        AddUserDialog dialog = addUserDialogProvider.get();
        dialog.setListener(listener);
        dialog.show(fm, "AddUserDialog");
    }

    /**
     * Displays a dialog to edit an existing user's personal details.
     *
     * @param fm       The {@link FragmentManager}.
     * @param user     The {@link User} object containing current data.
     * @param listener The listener to handle the update results.
     */
    @Override
    public void showEditUserDialog(FragmentManager fm, User user, EditUserDialog.EditUserDialogListener listener) {
        EditUserDialog dialog = editUserDialogProvider.get();
        dialog.setData(user, listener);
        dialog.show(fm, "EditUserDialog");
    }

    /**
     * Displays a dialog to rename an existing forum category.
     *
     * @param fm       The {@link FragmentManager}.
     * @param category The {@link ForumCategory} to rename.
     * @param listener The listener to handle the name update.
     */
    @Override
    public void showEditForumCategoryDialog(FragmentManager fm, ForumCategory category, EditForumCategoryDialog.EditForumCategoryDialogListener listener) {
        EditForumCategoryDialog dialog = editForumCategoryDialogProvider.get();
        dialog.setData(category, listener);
        dialog.show(fm, "EditForumCategoryDialog");
    }

    /**
     * Displays a full-screen image viewer.
     *
     * @param fm            The {@link FragmentManager}.
     * @param imageDrawable The {@link Drawable} to display in full screen.
     */
    @Override
    public void showFullImageDialog(FragmentManager fm, Drawable imageDrawable) {
        FullImageDialog dialog = fullImageDialogProvider.get();
        dialog.setImage(imageDrawable);
        dialog.show(fm, "FullImageDialog");
    }

    /**
     * Displays a dialog to choose a profile image source (Camera/Gallery) or delete it.
     *
     * @param fm       The {@link FragmentManager}.
     * @param hasImage Whether the user currently has an active profile image.
     * @param listener The listener to handle selection actions.
     */
    @Override
    public void showProfileImageDialog(FragmentManager fm, boolean hasImage, ProfileImageDialog.ImagePickerListener listener) {
        ProfileImageDialog dialog = profileImageDialogProvider.get();
        dialog.setData(hasImage, listener);
        dialog.show(fm, "ProfileImageDialog");
    }

    /**
     * Displays a dialog to add or edit an emergency contact.
     *
     * @param fm       The {@link FragmentManager}.
     * @param contact  The {@link EmergencyContact} to edit, or null for a new one.
     * @param listener The listener to handle the contact submission.
     */
    @Override
    public void showEmergencyContactDialog(FragmentManager fm, EmergencyContact contact, AddEmergencyContactDialog.AddEmergencyContactListener listener) {
        AddEmergencyContactDialog dialog = addEmergencyContactDialogProvider.get();
        dialog.setData(contact, listener);
        dialog.show(fm, "AddEmergencyContactDialog");
    }

    /**
     * Displays a standardized confirmation dialog with "Confirm" and optional "Cancel" buttons.
     *
     * @param fm          The {@link FragmentManager}.
     * @param title       The dialog title string.
     * @param message     The descriptive message body.
     * @param confirmText Text for the positive action button.
     * @param cancelText  Optional text for the negative button (if null, the button is hidden).
     * @param onConfirm   The Runnable to execute upon user confirmation.
     */
    @Override
    public void showConfirmDialog(FragmentManager fm, String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        ConfirmDialog dialog = confirmDialogProvider.get();
        dialog.setData(title, message, confirmText, cancelText, onConfirm);
        dialog.show(fm, "ConfirmDialog");
    }
}
