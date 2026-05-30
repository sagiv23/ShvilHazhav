package com.example.sagivproject.screens;

import static android.view.animation.AnimationUtils.loadAnimation;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.dialogs.FullImageDialog;
import com.example.sagivproject.dialogs.ProfileImageDialog;
import com.example.sagivproject.dialogs.UserDialog;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseCallback;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IUserService;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.ImageUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that displays and allows editing of detailed user profile information.
 * <p>
 * This screen provides a read-only view of primary user data (name, email, age, birthdate)
 * and features for:
 * <ul>
 * <li>Editing personal details via a centralized dialog.</li>
 * <li>Updating the profile image using the camera or device gallery.</li>
 * <li>Viewing the profile image in full screen.</li>
 * <li>Deleting the current profile image.</li>
 * </ul>
 * It ensures that any changes are synchronized with both the Firebase database and the local cache.
 * </p>
 */
@AndroidEntryPoint
public class DetailsAboutUserActivity extends BaseActivity {
    /**
     * Utility for handling image conversions and loading.
     */
    @Inject
    protected ImageUtil imageUtil;

    /**
     * Utility for date formatting.
     */
    @Inject
    protected CalendarUtil calendarUtil;

    @Inject
    protected IUserService userService;

    @Inject
    protected IAuthService authService;

    @Inject
    protected Provider<UserDialog> userDialogProvider;

    @Inject
    protected Provider<ProfileImageDialog> profileImageDialogProvider;

    @Inject
    protected Provider<FullImageDialog> fullImageDialogProvider;

    /**
     * UI components for displaying detailed user profile data.
     */
    private TextView txtTitle, txtEmail, txtPassword, txtAge, txtBirthDate;

    private ImageView imgUserProfile;

    /**
     * Local reference to the user's profile data.
     */
    private User user;

    /**
     * Launcher for the modern Android Photo Picker.
     */
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    /**
     * Launcher for capturing a temporary bitmap from the camera.
     */
    private ActivityResultLauncher<Void> cameraLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_details_about_user, R.id.detailsAboutUserPage);
        setupMenu();

        user = sharedPreferencesUtil.getUser();

        imgUserProfile = findViewById(R.id.img_DetailsAboutUser_user_profile);
        FloatingActionButton btnChangePhoto = findViewById(R.id.btn_DetailsAboutUser_change_photo);
        Button btnEditUser = findViewById(R.id.btn_DetailsAboutUser_edit_user);

        txtTitle = findViewById(R.id.txt_DetailsAboutUser_title);
        txtAge = findViewById(R.id.txt_DetailsAboutUser_age);
        txtBirthDate = findViewById(R.id.txt_DetailsAboutUser_birth_date);
        txtEmail = findViewById(R.id.txt_DetailsAboutUser_email);
        txtPassword = findViewById(R.id.txt_DetailsAboutUser_password);

        btnEditUser.setOnClickListener(v -> openEditDialog());
        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        imgUserProfile.setOnClickListener(v -> {
            if (user != null && user.getProfileImage() != null) {
                FullImageDialog dialog = fullImageDialogProvider.get();
                dialog.setImage(imgUserProfile.getDrawable());
                dialog.show(getSupportFragmentManager(), "FullImageDialog");
            }
        });

        setupLaunchers();

        findViewById(R.id.btn_DetailsAboutUser_change_photo).startAnimation(loadAnimation(this, R.anim.floating_animation));
    }

    /**
     * Initializes activity result launchers for camera and gallery interactions.
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
                            Bitmap bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri));
                            if (bitmap != null) {
                                handleImageBitmap(bitmap);
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "שגיאה בטעינת התמונה", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    /**
     * Handles the results of permission requests, specifically for the camera and gallery.
     *
     * @param isGranted Map of requested permissions and their granted status.
     */
    @Override
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
        if (isGranted.containsKey(Manifest.permission.CAMERA) && !Boolean.TRUE.equals(isGranted.get(Manifest.permission.CAMERA))) {
            Toast.makeText(this, "נדרשת הרשאת מצלמה כדי לצלם תמונה", Toast.LENGTH_SHORT).show();
        } else if ((isGranted.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE) || isGranted.containsKey(Manifest.permission.READ_MEDIA_IMAGES)) &&
                !(Boolean.TRUE.equals(isGranted.get(Manifest.permission.READ_EXTERNAL_STORAGE)) || Boolean.TRUE.equals(isGranted.get(Manifest.permission.READ_MEDIA_IMAGES)))) {
            Toast.makeText(this, "ההרשאה נדחתה. לא ניתן להמשיך בפעולה.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refreshes user data from the database whenever the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserFromDatabase();
    }

    /**
     * Fetches the latest user profile from the database to ensure UI consistency.
     */
    private void loadUserFromDatabase() {
        if (user == null) return;
        showLoading();
        userService.getUser(user.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User dbUser) {
                hideLoading();
                if (dbUser != null) {
                    user = dbUser;
                    sharedPreferencesUtil.saveUser(user);
                    loadUserDetailsToUI();
                }
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Populates UI components with the current user's data.
     */
    private void loadUserDetailsToUI() {
        if (user == null) {
            return;
        }

        txtTitle.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(user.getPassword());

        imageUtil.loadImage(user.getProfileImage(), imgUserProfile);

        int age = user.getAge();
        txtAge.setText(age == -1 ? "לא ידוע" : String.valueOf(age));

        String birthDateStr = user.getBirthDate();
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            birthDateStr = calendarUtil.formatDbDateToDisplay(birthDateStr);
        } else {
            birthDateStr = "לא הוזן";
        }

        txtBirthDate.setText(birthDateStr);
    }

    /**
     * Displays the dialog for editing user profile details.
     */
    private void openEditDialog() {
        UserDialog dialog = userDialogProvider.get();
        dialog.setData(user, updatedUser -> {
            showLoading();
            authService.updateUser(updatedUser, updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getBirthDate(), updatedUser.getEmail(), updatedUser.getPassword(), new IAuthService.UpdateUserCallback() {
                @Override
                public void onSuccess(User resultUser) {
                    hideLoading();
                    Toast.makeText(DetailsAboutUserActivity.this, "פרטי המשתמש עודכנו", Toast.LENGTH_SHORT).show();
                    sharedPreferencesUtil.saveUser(resultUser);
                    user = resultUser;
                    loadUserDetailsToUI();
                }

                @Override
                public void onError(String message) {
                    hideLoading();
                    Toast.makeText(DetailsAboutUserActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "EditUserDialog");
    }

    /**
     * Launches the system photo picker.
     */
    private void launchGallery() {
        photoPickerLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    /**
     * Displays the image picker dialog to choose between camera, gallery, or deletion.
     */
    private void openImagePicker() {
        boolean hasImage = user.getProfileImage() != null && !user.getProfileImage().isEmpty();

        ProfileImageDialog dialog = profileImageDialogProvider.get();
        dialog.setData(hasImage, new ProfileImageDialog.ImagePickerListener() {
            @Override
            public void onCamera() {
                runWithPermission(Manifest.permission.CAMERA, () -> cameraLauncher.launch(null));
            }

            @Override
            public void onGallery() {
                runWithPermission(Manifest.permission.READ_MEDIA_IMAGES, DetailsAboutUserActivity.this::launchGallery);
            }

            @Override
            public void onDelete() {
                deleteProfileImage();
            }
        });
        dialog.show(getSupportFragmentManager(), "ProfileImageDialog");
    }

    /**
     * Removes the profile image from the user's account.
     */
    private void deleteProfileImage() {
        user.setProfileImage(null);
        imgUserProfile.setImageResource(R.drawable.ic_user);

        showLoading();
        userService.updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                hideLoading();
                sharedPreferencesUtil.saveUser(user);
                Toast.makeText(DetailsAboutUserActivity.this, "תמונת הפרופיל נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה במחיקת התמונה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Processes a new image bitmap, converts it to Base64, and updates the profile.
     *
     * @param bitmap The new image bitmap.
     */
    private void handleImageBitmap(Bitmap bitmap) {
        imgUserProfile.setImageBitmap(bitmap);
        String base64 = imageUtil.convertTo64Base(imgUserProfile);
        user.setProfileImage(base64);
        saveProfileImage();
    }

    /**
     * Commits the updated profile image string to the database.
     */
    private void saveProfileImage() {
        showLoading();
        userService.updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                hideLoading();
                sharedPreferencesUtil.saveUser(user);
                Toast.makeText(DetailsAboutUserActivity.this, "תמונת הפרופיל עודכנה!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בעדכון התמונה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}