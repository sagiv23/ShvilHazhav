package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.example.sagivproject.workers.MedicationWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

//למחוק בסיום הפרויקט
/*
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.services.DatabaseService;
import java.io.ByteArrayOutputStream;
 */

public class MainActivity extends BaseActivity {
    Button btnToContact, btnToDetailsAboutUser, btnToMedicationList, btnToForum, btnToAi, btnToGameHomeScreen, btnToExit;
    private TextView txtHomePageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        User user = SharedPreferencesUtil.getUser(this);

        checkNotificationPermission();
        setupDailyNotifications();

        btnToContact = findViewById(R.id.btn_main_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_main_to_DetailsAboutUser);
        btnToMedicationList = findViewById(R.id.btn_main_to_MedicationList);
        btnToForum = findViewById(R.id.btn_main_to_forum);
        btnToAi = findViewById(R.id.btn_main_to_Ai);
        btnToGameHomeScreen = findViewById(R.id.btn_main_to_GameHomeScreen);
        btnToExit = findViewById(R.id.btn_main_to_exit);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DetailsAboutUserActivity.class)));
        btnToMedicationList.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MedicationListActivity.class)));
        btnToForum.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ForumActivity.class)));
        btnToAi.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AiActivity.class)));
        btnToGameHomeScreen.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, GameHomeScreenActivity.class)));
        btnToExit.setOnClickListener(view -> logout());

        txtHomePageTitle = findViewById(R.id.txt_main_Title);
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            txtHomePageTitle.setText("שלום מטופל יקר");
        } else {
            txtHomePageTitle.setText("שלום " + user.getFullName());
        }

        //להעלאת תמונות - למחוק בסוף הפרויקט!
        //uploadGameImagesIfNeeded();
    }

    //התראות לגבי התרופות
    private void setupDailyNotifications() {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();

        dueDate.set(Calendar.HOUR_OF_DAY, 8);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.set(Calendar.MILLISECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        PeriodicWorkRequest notificationRequest =
                new PeriodicWorkRequest.Builder(
                        MedicationWorker.class,
                        24, TimeUnit.HOURS)
                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                        .addTag("MedicationWorkTag")
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(true)
                                .build())
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MedicationDailyWork",
                ExistingPeriodicWorkPolicy.KEEP,
                notificationRequest
        );
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // זה יקפיץ את הדיאלוג הרשמי של אנדרואיד
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }




    //העלאת כל התמונות - למחוק בסוף הפרויקט!
    /*
    private void uploadGameImagesIfNeeded() {
        databaseService.imagesExist(new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    // כבר קיימות תמונות – לא עושים כלום
                    return;
                }

                uploadAllImages();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MainActivity.this,
                        "שגיאה בבדיקת תמונות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAllImages() {

        int[] imageIds = {
                R.drawable.pics_for_game_1,
                R.drawable.pics_for_game_2,
                R.drawable.pics_for_game_3,
                R.drawable.pics_for_game_4,
                R.drawable.pics_for_game_5,
                R.drawable.pics_for_game_6,
                R.drawable.pics_for_game_7,
                R.drawable.pics_for_game_8,
                R.drawable.pics_for_game_9,
                R.drawable.pics_for_game_10,
                R.drawable.pics_for_game_11,
                R.drawable.pics_for_game_12,
                R.drawable.pics_for_game_13,
                R.drawable.pics_for_game_14,
                R.drawable.pics_for_game_15,
                R.drawable.pics_for_game_16,
                R.drawable.pics_for_game_17,
                R.drawable.pics_for_game_18,
                R.drawable.pics_for_game_19,
                R.drawable.pics_for_game_20,
                R.drawable.pics_for_game_21,
                R.drawable.pics_for_game_22,
                R.drawable.pics_for_game_23,
                R.drawable.pics_for_game_24,
                R.drawable.pics_for_game_25,
                R.drawable.pics_for_game_26,
                R.drawable.pics_for_game_27,
                R.drawable.pics_for_game_28,
                R.drawable.pics_for_game_29,
                R.drawable.pics_for_game_30,
                R.drawable.pics_for_game_31,
                R.drawable.pics_for_game_32,
                R.drawable.pics_for_game_33,
                R.drawable.pics_for_game_34,
                R.drawable.pics_for_game_35,
                R.drawable.pics_for_game_36,
                R.drawable.pics_for_game_37,
                R.drawable.pics_for_game_38,
                R.drawable.pics_for_game_39,
                R.drawable.pics_for_game_40,
                R.drawable.pics_for_game_41,
                R.drawable.pics_for_game_42,
                R.drawable.pics_for_game_43,
                R.drawable.pics_for_game_44,
                R.drawable.pics_for_game_45,
                R.drawable.pics_for_game_46,
                R.drawable.pics_for_game_47,
                R.drawable.pics_for_game_48,
                R.drawable.pics_for_game_49,
                R.drawable.pics_for_game_50,
                R.drawable.pics_for_game_51,
                R.drawable.pics_for_game_52,
                R.drawable.pics_for_game_53,
                R.drawable.pics_for_game_54,
                R.drawable.pics_for_game_55,
                R.drawable.pics_for_game_56,
                R.drawable.pics_for_game_57,
                R.drawable.pics_for_game_58,
                R.drawable.pics_for_game_59,
                R.drawable.pics_for_game_60,
                R.drawable.pics_for_game_61,
                R.drawable.pics_for_game_62,
                R.drawable.pics_for_game_63,
                R.drawable.pics_for_game_64,
                R.drawable.pics_for_game_65,
                R.drawable.pics_for_game_66,
                R.drawable.pics_for_game_67
        };


        for (int i = 0; i < imageIds.length; i++) {

            Bitmap original = BitmapFactory.decodeResource(
                    getResources(),
                    imageIds[i]
            );

            Bitmap resized = resize(original, 512);
            String base64 = bitmapToBase64(resized);

            String cardId = "card" + (i + 1);
            ImageData image = new ImageData(cardId, base64);

            databaseService.createImage(image, null);
        }
    }

    private Bitmap resize(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = (float) width / height;

        if (ratio > 1) {
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            height = maxSize;
            width = (int) (height * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // JPEG + איכות 70% = חיסכון משמעותי
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
     */
}