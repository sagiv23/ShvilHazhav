package com.example.sagivproject.screens;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.base.BaseActivity;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.ImageUtil;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;

public class DetailsAboutUserActivity extends BaseActivity {
    private Button btnToMain, btnToDetailsAboutUser, btnToContact, btnToExit, btnToAdmin, btnEditUser;
    private View separatorLine;
    private LinearLayout topMenu;
    private TextView txtTitle, txtFirstName, txtLastName, txtEmail, txtPassword;
    private ImageView imgUserProfile;
    private Button btnChangePhoto;

    private static final int REQ_CAMERA = 100;
    private static final int REQ_GALLERY = 200;

    private User user;

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

        PagePermissions.onlyUserAndAdmin(this);

        user = SharedPreferencesUtil.getUser(this);

        topMenu = findViewById(R.id.topMenuDetailsAboutUser);
        //משתמש מחובר
        btnToMain = findViewById(R.id.btn_DetailsAboutUser_to_main);
        btnToDetailsAboutUser = findViewById(R.id.btn_DetailsAboutUser_to_DetailsAboutUserPage);
        btnToContact = findViewById(R.id.btn_DetailsAboutUser_to_contact);
        btnToExit = findViewById(R.id.btn_DetailsAboutUser_to_exit);
        separatorLine = findViewById(R.id.separatorLine_DetailsAboutUser);

        //מנהל
        btnToAdmin = findViewById(R.id.btn_DetailsAboutUser_to_admin);

        boolean isAdmin = user.getIsAdmin();

        if (isAdmin) {
            //הופך את כפתורי המנהל ל-VISIBLE
            btnToAdmin.setVisibility(View.VISIBLE);
            topMenu.setVisibility(View.GONE);
            separatorLine.setVisibility(View.GONE);
        }
        else {
            //הופך את כפתורי המשתמש המחובר ל-VISIBLE
            btnToMain.setVisibility(View.VISIBLE);
            btnToDetailsAboutUser.setVisibility(View.VISIBLE);
            btnToContact.setVisibility(View.VISIBLE);
            btnToExit.setVisibility(View.VISIBLE);
            separatorLine.setVisibility(View.VISIBLE);
            topMenu.setVisibility(View.VISIBLE);
        }

        btnToMain.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        btnToContact.setOnClickListener(v -> startActivity(new Intent(this, ContactActivity.class)));
        btnToExit.setOnClickListener(v -> logout());
        btnToAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminPageActivity.class)));

        btnEditUser = findViewById(R.id.btn_DetailsAboutUser_edit_user);
        btnEditUser.setOnClickListener(v -> openEditDialog());

        imgUserProfile = findViewById(R.id.img_DetailsAboutUser_user_profile);
        btnChangePhoto = findViewById(R.id.btn_DetailsAboutUser_change_photo);
        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        imgUserProfile.setOnClickListener(v -> showFullImageDialog());

        txtTitle = findViewById(R.id.txt_DetailsAboutUser_title);
        txtFirstName = findViewById(R.id.txt_DetailsAboutUser_first_name);
        txtLastName = findViewById(R.id.txt_DetailsAboutUser_last_name);
        txtEmail = findViewById(R.id.txt_DetailsAboutUser_email);
        txtPassword = findViewById(R.id.txt_DetailsAboutUser_password);

        loadUserDetailsFromSharedPref();
    }

    private void loadUserDetailsFromSharedPref() {
        txtTitle.setText(user.getFullName());
        txtFirstName.setText(user.getFirstName());
        txtLastName.setText(user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(user.getPassword());

        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Bitmap bmp = ImageUtil.convertFrom64base(user.getProfileImage());
            if (bmp != null) imgUserProfile.setImageBitmap(bmp);
        }
    }

    private void openEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        builder.setView(dialogView);

        EditText inputFirstName = dialogView.findViewById(R.id.inputEditUserFirstName);
        EditText inputLastName = dialogView.findViewById(R.id.inputEditUserLastName);
        EditText inputPassword = dialogView.findViewById(R.id.inputEditUserPassword);
        Button btnSave = dialogView.findViewById(R.id.btnEditUserSave);
        Button btnCancel = dialogView.findViewById(R.id.btnEditUserCancel);

        inputFirstName.setText(user.getFirstName());
        inputLastName.setText(user.getLastName());
        inputPassword.setText(user.getPassword());

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newFirst = inputFirstName.getText().toString().trim();
            String newLast = inputLastName.getText().toString().trim();
            String newPass = inputPassword.getText().toString().trim();

            if (newFirst.isEmpty() || newLast.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setFirstName(newFirst);
            user.setLastName(newLast);
            user.setPassword(newPass);

            updateUserInDatabaseAndSharedPreference();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUserInDatabaseAndSharedPreference() {
        DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                txtFirstName.setText(user.getFirstName());
                txtLastName.setText(user.getLastName());
                txtPassword.setText(user.getPassword());
                txtTitle.setText(user.getFullName());

                SharedPreferencesUtil.saveUser(DetailsAboutUserActivity.this, user);

                Toast.makeText(DetailsAboutUserActivity.this, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בעדכון הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        ImageUtil.requestPermission(this);

        String[] options = {"צלם תמונה", "בחר מהגלריה"};

        new AlertDialog.Builder(this)
                .setTitle("בחר תמונת פרופיל")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Camera
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQ_CAMERA);
                    } else { // Gallery
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, REQ_GALLERY);
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        Bitmap bitmap = null;

        if (requestCode == REQ_CAMERA && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
        }
        else if (requestCode == REQ_GALLERY && data != null) {
            try {
                bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(data.getData())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (bitmap != null) {
            imgUserProfile.setImageBitmap(bitmap);

            //המרה ל־Base64 ושמירה
            String base64 = ImageUtil.convertTo64Base(imgUserProfile);
            user.setProfileImage(base64);

            saveProfileImage();
        }
    }

    private void saveProfileImage() {
        DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                SharedPreferencesUtil.saveUser(DetailsAboutUserActivity.this, user);

                Toast.makeText(DetailsAboutUserActivity.this, "תמונת הפרופיל עודכנה!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בעדכון התמונה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFullImageDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);

        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);

        //מציב את התמונה שיש בתמונה המקורית
        dialogImage.setImageDrawable(imgUserProfile.getDrawable());

        //לוחצים על התמונה - יוצא
        dialogImage.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}