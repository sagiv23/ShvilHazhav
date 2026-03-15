package com.example.sagivproject.screens;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.ProfileImageDialog;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.utils.ImageUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment that displays and allows management of user profile details.
 * <p>
 * This fragment provides features for viewing personal information (name, email, age, birthdate),
 * editing these details via a dialog, and updating the profile image using the camera or gallery.
 * </p>
 */
@AndroidEntryPoint
public class DetailsAboutUserFragment extends BaseFragment {
    /**
     * Utility for handling image conversions and loading.
     */
    @Inject
    protected ImageUtil imageUtil;

    private TextView txtTitle, txtEmail, txtPassword, txtAge, txtBirthDate;
    private ImageView imgUserProfile;
    private User user;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_about_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = sharedPreferencesUtil.getUser();

        imgUserProfile = view.findViewById(R.id.img_DetailsAboutUser_user_profile);
        FloatingActionButton btnChangePhoto = view.findViewById(R.id.btn_DetailsAboutUser_change_photo);
        Button btnEditUser = view.findViewById(R.id.btn_DetailsAboutUser_edit_user);

        txtTitle = view.findViewById(R.id.txt_DetailsAboutUser_title);
        txtAge = view.findViewById(R.id.txt_DetailsAboutUser_age);
        txtBirthDate = view.findViewById(R.id.txt_DetailsAboutUser_birth_date);
        txtEmail = view.findViewById(R.id.txt_DetailsAboutUser_email);
        txtPassword = view.findViewById(R.id.txt_DetailsAboutUser_password);

        btnEditUser.setOnClickListener(v -> openEditDialog());
        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        imgUserProfile.setOnClickListener(v -> {
            if (user != null && user.getProfileImage() != null) {
                dialogService.showFullImageDialog(getParentFragmentManager(), imgUserProfile.getDrawable());
            }
        });

        setupLaunchers();
        loadUserDetailsToUI();
    }

    /**
     * Initializes the ActivityResultLaunchers for camera and photo picking.
     */
    private void setupLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        handleImageBitmap(bitmap);
                    }
                }
        );

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeStream(
                                    requireContext().getContentResolver().openInputStream(uri)
                            );
                            if (bitmap != null) {
                                handleImageBitmap(bitmap);
                            }
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "שגיאה בטעינת התמונה", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
        if (Boolean.TRUE.equals(isGranted.get(Manifest.permission.CAMERA))) {
            cameraLauncher.launch(null);
        } else {
            Toast.makeText(requireContext(), "נדרשת הרשאת מצלמה כדי לצלם תמונה", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserFromDatabase();
    }

    /**
     * Fetches the latest user data from the database.
     */
    private void loadUserFromDatabase() {
        if (user == null) return;
        databaseService.getUserService().getUser(user.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User dbUser) {
                if (dbUser != null) {
                    user = dbUser;
                    sharedPreferencesUtil.saveUser(user);
                    loadUserDetailsToUI();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Populates the UI components with the current user's details.
     */
    private void loadUserDetailsToUI() {
        if (user == null || !isAdded()) {
            return;
        }

        txtTitle.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(user.getPassword());

        imageUtil.loadImage(user.getProfileImage(), imgUserProfile);

        int age = user.getAge();
        txtAge.setText(String.valueOf(age));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(user.getBirthDateMillis());

        String birthDate = String.format(
                Locale.ROOT,
                "%02d/%02d/%04d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR)
        );

        txtBirthDate.setText(birthDate);
    }

    /**
     * Opens a dialog to edit user personal details.
     */
    private void openEditDialog() {
        dialogService.showEditUserDialog(getParentFragmentManager(), user, (fName, lName, birthDate, email, password) ->
                databaseService.getAuthService().updateUser(user, fName, lName, birthDate, email, password, new IAuthService.UpdateUserCallback() {
                    @Override
                    public void onSuccess(User updatedUser) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "הפרטים עודכנו!", Toast.LENGTH_SHORT).show();
                            sharedPreferencesUtil.saveUser(updatedUser);
                            user = updatedUser;
                            loadUserDetailsToUI();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                        }
                    }
                }));
    }

    /**
     * Opens a dialog to choose between camera and gallery for updating the profile image.
     */
    private void openImagePicker() {
        boolean hasImage = user.getProfileImage() != null && !user.getProfileImage().isEmpty();

        dialogService.showProfileImageDialog(getParentFragmentManager(), hasImage, new ProfileImageDialog.ImagePickerListener() {
            @Override
            public void onCamera() {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(null);
                } else {
                    requestPermissions(Manifest.permission.CAMERA);
                }
            }

            @Override
            public void onGallery() {
                photoPickerLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );
            }

            @Override
            public void onDelete() {
                deleteProfileImage();
            }
        });
    }

    /**
     * Deletes the user's profile image and updates the database.
     */
    private void deleteProfileImage() {
        user.setProfileImage(null);
        imgUserProfile.setImageResource(R.drawable.ic_user);

        databaseService.getUserService().updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                sharedPreferencesUtil.saveUser(user);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "תמונת הפרופיל נמחקה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "שגיאה במחיקת התמונה", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Handles a new image bitmap, converts it to Base64, and triggers a save operation.
     *
     * @param bitmap The new profile image bitmap.
     */
    private void handleImageBitmap(Bitmap bitmap) {
        imgUserProfile.setImageBitmap(bitmap);
        String base64 = imageUtil.convertTo64Base(imgUserProfile);
        user.setProfileImage(base64);
        saveProfileImage();
    }

    /**
     * Saves the updated user profile image to the database.
     */
    private void saveProfileImage() {
        databaseService.getUserService().updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                sharedPreferencesUtil.saveUser(user);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "תמונת הפרופיל עודכנה!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "שגיאה בעדכון התמונה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
