package com.example.sagivproject.services.impl;

import android.graphics.drawable.Drawable;

import androidx.fragment.app.FragmentManager;

import com.example.sagivproject.dialogs.ConfirmDialog;
import com.example.sagivproject.dialogs.EditForumCategoryDialog;
import com.example.sagivproject.dialogs.EmergencyContactDialog;
import com.example.sagivproject.dialogs.FullImageDialog;
import com.example.sagivproject.dialogs.LoadingDialog;
import com.example.sagivproject.dialogs.MedicationDialog;
import com.example.sagivproject.dialogs.ProfileImageDialog;
import com.example.sagivproject.dialogs.TipDialog;
import com.example.sagivproject.dialogs.UserDialog;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDialogService;

import java.util.List;

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
    private final Provider<UserDialog> userDialogProvider;
    private final Provider<EditForumCategoryDialog> editForumCategoryDialogProvider;
    private final Provider<FullImageDialog> fullImageDialogProvider;
    private final Provider<ProfileImageDialog> profileImageDialogProvider;
    private final Provider<ConfirmDialog> confirmDialogProvider;
    private final Provider<EmergencyContactDialog> addEmergencyContactDialogProvider;
    private final Provider<TipDialog> tipDialogProvider;
    private final Provider<LoadingDialog> loadingDialogProvider;

    private LoadingDialog currentLoadingDialog;

    /**
     * Constructs a new DialogService with providers for all dialog fragments.
     */
    @Inject
    public DialogService(
            Provider<MedicationDialog> medicationDialogProvider,
            Provider<UserDialog> userDialogProvider,
            Provider<EditForumCategoryDialog> editForumCategoryDialogProvider,
            Provider<FullImageDialog> fullImageDialogProvider,
            Provider<ProfileImageDialog> profileImageDialogProvider,
            Provider<ConfirmDialog> confirmDialogProvider,
            Provider<EmergencyContactDialog> addEmergencyContactDialogProvider,
            Provider<TipDialog> tipDialogProvider,
            Provider<LoadingDialog> loadingDialogProvider
    ) {
        this.medicationDialogProvider = medicationDialogProvider;
        this.userDialogProvider = userDialogProvider;
        this.editForumCategoryDialogProvider = editForumCategoryDialogProvider;
        this.fullImageDialogProvider = fullImageDialogProvider;
        this.profileImageDialogProvider = profileImageDialogProvider;
        this.confirmDialogProvider = confirmDialogProvider;
        this.addEmergencyContactDialogProvider = addEmergencyContactDialogProvider;
        this.tipDialogProvider = tipDialogProvider;
        this.loadingDialogProvider = loadingDialogProvider;
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
     * Displays a dialog for administrators to create a new user profile or edit an existing one.
     *
     * @param fm       The {@link FragmentManager}.
     * @param user     The {@link User} object to edit, or null to add a new one.
     * @param listener The listener to handle the user creation or update logic.
     */
    @Override
    public void showUserDialog(FragmentManager fm, User user, UserDialog.UserDialogListener listener) {
        UserDialog dialog = userDialogProvider.get();
        dialog.setData(user, listener);
        dialog.show(fm, "UserDialog");
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
    public void showEmergencyContactDialog(FragmentManager fm, EmergencyContact contact, EmergencyContactDialog.AddEmergencyContactListener listener) {
        EmergencyContactDialog dialog = addEmergencyContactDialogProvider.get();
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

    /**
     * Displays a dialog to add a new tip of the day or edit an existing one.
     *
     * @param fm            The {@link FragmentManager}.
     * @param tip           The {@link TipOfTheDay} to edit, or null for a new one.
     * @param existingDates List of date IDs that already have a tip.
     * @param listener      The listener to handle the tip submission.
     */
    @Override
    public void showTipDialog(FragmentManager fm, TipOfTheDay tip, List<String> existingDates, TipDialog.TipDialogListener listener) {
        TipDialog dialog = tipDialogProvider.get();
        dialog.setInspirationMode(false);
        dialog.setData(tip, existingDates, listener);
        dialog.show(fm, "TipDialog");
    }

    @Override
    public void showInspirationDialog(FragmentManager fm, TipOfTheDay inspiration, TipDialog.TipDialogListener listener) {
        TipDialog dialog = tipDialogProvider.get();
        dialog.setInspirationMode(true);
        dialog.setData(inspiration, listener);
        dialog.show(fm, "InspirationDialog");
    }

    /**
     * Displays a non-cancelable loading dialog.
     *
     * @param fm The {@link FragmentManager}.
     */
    @Override
    public void showLoadingDialog(FragmentManager fm) {
        if (currentLoadingDialog == null || !currentLoadingDialog.isAdded()) {
            currentLoadingDialog = loadingDialogProvider.get();
            currentLoadingDialog.show(fm, "LoadingDialog");
        }
    }

    /**
     * Hides the currently displayed loading dialog.
     */
    @Override
    public void hideLoadingDialog() {
        if (currentLoadingDialog != null && currentLoadingDialog.isAdded()) {
            currentLoadingDialog.dismiss();
            currentLoadingDialog = null;
        }
    }
}