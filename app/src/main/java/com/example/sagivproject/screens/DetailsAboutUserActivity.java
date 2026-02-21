package com.example.sagivproject.screens;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.EditUserDialog;
import com.example.sagivproject.screens.dialogs.FullImageDialog;
import com.example.sagivproject.screens.dialogs.ProfileImageDialog;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.utils.ImageUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity to display and manage user profile details.
 * <p>
 * This screen shows the current user's information, including their name, email, age,
 * birthdate, and game statistics. It allows the user to edit their profile details
 * and change their profile picture.
 * </p>
 */
@AndroidEntryPoint
public class DetailsAboutUserActivity extends BaseActivity {
    private TextView txtTitle, txtEmail, txtPassword, txtAge, txtBirthDate, txtWins;
    private ImageView imgUserProfile;
    private User user;
    private ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> photoPickerLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;

    /**
     * Initializes the activity, sets up the UI, and configures event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_about_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsAboutUserPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = sharedPreferencesUtil.getUser();

        assert user != null;
        boolean isAdmin = user.isAdmin();

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        if (!isAdmin) {
            View separatorLine = findViewById(R.id.separatorLine);
            separatorLine.setVisibility(View.VISIBLE);
        }

        Button btnEditUser = findViewById(R.id.btn_DetailsAboutUser_edit_user);
        btnEditUser.setOnClickListener(v -> openEditDialog());

        imgUserProfile = findViewById(R.id.img_DetailsAboutUser_user_profile);
        FloatingActionButton btnChangePhoto = findViewById(R.id.btn_DetailsAboutUser_change_photo);
        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        imgUserProfile.setOnClickListener(v -> {
            if (user.getProfileImage() != null) {
                new FullImageDialog(
                        this,
                        imgUserProfile.getDrawable()
                ).show();
            }
        });

        txtTitle = findViewById(R.id.txt_DetailsAboutUser_title);
        txtAge = findViewById(R.id.txt_DetailsAboutUser_age);
        txtBirthDate = findViewById(R.id.txt_DetailsAboutUser_birth_date);
        txtWins = findViewById(R.id.txt_DetailsAboutUser_wins);
        txtEmail = findViewById(R.id.txt_DetailsAboutUser_email);
        txtPassword = findViewById(R.id.txt_DetailsAboutUser_password);

        loadUserDetailsToUI();

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
                                    getContentResolver().openInputStream(uri)
                            );
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
     * Reloads user data from the database when the activity resumes to ensure the
     * displayed information is up-to-date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserFromDatabase();
    }

    /**
     * Fetches the latest user data from the database and updates the local user object
     * and UI.
     */
    private void loadUserFromDatabase() {
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
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Populates the UI fields with the details from the local user object.
     */
    private void loadUserDetailsToUI() {
        if (user == null) {
            return;
        }
        txtTitle.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(user.getPassword());

        ImageUtil.loadImage(user.getProfileImage(), imgUserProfile);

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

        txtWins.setText(String.valueOf(user.getCountWins()));
    }

    /**
     * Opens a dialog to allow the user to edit their profile information.
     */
    private void openEditDialog() {
        new EditUserDialog(this, user, () -> {
            sharedPreferencesUtil.saveUser(user);
            loadUserDetailsToUI();
        }, databaseService.getAuthService()).show();
    }

    /**
     * Opens a dialog to allow the user to choose a new profile image from the camera or
     * gallery, or to delete the current image.
     */
    private void openImagePicker() {
        boolean hasImage = user.getProfileImage() != null && !user.getProfileImage().isEmpty();

        new ProfileImageDialog(this, hasImage, new ProfileImageDialog.ImagePickerListener() {
            @Override
            public void onCamera() {
                cameraLauncher.launch(null);
            }

            @Override
            public void onGallery() {
                photoPickerLauncher.launch(
                        new androidx.activity.result.PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );
            }

            @Override
            public void onDelete() {
                deleteProfileImage();
            }
        }).show();
    }

    /**
     * Deletes the user's current profile image from the database and updates the UI.
     */
    private void deleteProfileImage() {
        user.setProfileImage(null);

        imgUserProfile.setImageResource(R.drawable.ic_user);

        databaseService.getUserService().updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                sharedPreferencesUtil.saveUser(user);
                Toast.makeText(DetailsAboutUserActivity.this, "תמונת הפרופיל נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה במחיקת התמונה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the selected image bitmap, sets it as the profile image view,
     * converts it to Base64, and saves it.
     *
     * @param bitmap The bitmap of the selected image.
     */
    private void handleImageBitmap(Bitmap bitmap) {
        imgUserProfile.setImageBitmap(bitmap);

        // Convert to Base64 and save
        String base64 = ImageUtil.convertTo64Base(imgUserProfile);
        user.setProfileImage(base64);

        saveProfileImage();
    }

    /**
     * Saves the updated user profile (with the new image) to the database.
     */
    private void saveProfileImage() {
        databaseService.getUserService().updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                sharedPreferencesUtil.saveUser(user);

                Toast.makeText(DetailsAboutUserActivity.this, "תמונת הפרופיל עודכנה!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בעדכון התמונה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
