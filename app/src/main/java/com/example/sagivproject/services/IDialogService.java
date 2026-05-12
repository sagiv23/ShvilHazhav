package com.example.sagivproject.services;

import android.graphics.drawable.Drawable;

import androidx.fragment.app.FragmentManager;

import com.example.sagivproject.dialogs.EditForumCategoryDialog;
import com.example.sagivproject.dialogs.EmergencyContactDialog;
import com.example.sagivproject.dialogs.MedicationDialog;
import com.example.sagivproject.dialogs.ProfileImageDialog;
import com.example.sagivproject.dialogs.UserDialog;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;

/**
 * Interface for managing the creation and display of all dialog fragments in the application.
 * <p>
 * This service provides a unified API to trigger complex UI dialogs across the app,
 * such as medication management, user editing, and confirmation prompts.
 * </p>
 */
public interface IDialogService {
    /**
     * Displays a dialog to add a new medication or edit an existing one.
     *
     * @param fm        The {@link FragmentManager} used to show the dialog.
     * @param medToEdit The {@link Medication} object to edit, or null to add a new one.
     * @param listener  The listener to handle submission events.
     */
    void showMedicationDialog(FragmentManager fm, Medication medToEdit, MedicationDialog.OnMedicationSubmitListener listener);

    /**
     * Displays a dialog for administrators to create a new user profile or edit an existing one.
     *
     * @param fm       The {@link FragmentManager}.
     * @param user     The {@link User} object to edit, or null to add a new one.
     * @param listener The listener to handle the user creation or update logic.
     */
    void showUserDialog(FragmentManager fm, User user, UserDialog.UserDialogListener listener);

    /**
     * Displays a dialog to rename an existing forum category.
     *
     * @param fm       The {@link FragmentManager}.
     * @param category The {@link ForumCategory} to rename.
     * @param listener The listener to handle the name update.
     */
    void showEditForumCategoryDialog(FragmentManager fm, ForumCategory category, EditForumCategoryDialog.EditForumCategoryDialogListener listener);

    /**
     * Displays a full-screen image viewer.
     *
     * @param fm            The {@link FragmentManager}.
     * @param imageDrawable The {@link Drawable} to display in full screen.
     */
    void showFullImageDialog(FragmentManager fm, Drawable imageDrawable);

    /**
     * Displays a dialog to choose a profile image source (Camera/Gallery) or delete it.
     *
     * @param fm       The {@link FragmentManager}.
     * @param hasImage Whether the user currently has an active profile image.
     * @param listener The listener to handle selection actions.
     */
    void showProfileImageDialog(FragmentManager fm, boolean hasImage, ProfileImageDialog.ImagePickerListener listener);

    /**
     * Displays a dialog to add or edit an emergency contact.
     *
     * @param fm       The {@link FragmentManager}.
     * @param contact  The {@link EmergencyContact} to edit, or null for a new one.
     * @param listener The listener to handle the contact submission.
     */
    void showEmergencyContactDialog(FragmentManager fm, EmergencyContact contact, EmergencyContactDialog.AddEmergencyContactListener listener);

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
    void showConfirmDialog(FragmentManager fm, String title, String message, String confirmText, String cancelText, Runnable onConfirm);
}