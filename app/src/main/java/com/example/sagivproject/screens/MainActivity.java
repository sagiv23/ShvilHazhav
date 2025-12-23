package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.example.sagivproject.workers.MedicationWorker;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button btnToContact, btnToDetailsAboutUser, btnToMedicationList, btnToForum, btnToAi, btnToGameHomeScreen, btnToExit;
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

        DatabaseService.getInstance().getUser(Objects.requireNonNull(SharedPreferencesUtil.getUserId(this)), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser == null) {
                    failedToGetUser();
                    return;
                }
                SharedPreferencesUtil.saveUser(MainActivity.this, updatedUser);
            }

            @Override
            public void onFailed(Exception e) {
                failedToGetUser();
            }

            private void failedToGetUser() {
                SharedPreferencesUtil.signOutUser(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        PagePermissions.checkUserPage(this);
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
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(MainActivity.this));

        txtHomePageTitle = findViewById(R.id.txt_main_Title);

        User user = SharedPreferencesUtil.getUser(this);
        showUserName(user);
    }

    private void showUserName(User user) {
        String fullName = user.getFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            txtHomePageTitle.setText("שלום מטופל יקר");
        } else {
            txtHomePageTitle.setText("שלום " + fullName);
        }
    }

    //התראות לגבי התרופות
    private void setupDailyNotifications() {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();

        //קביעת השעה ל-09:15 בדיוק
        dueDate.set(Calendar.HOUR_OF_DAY, 9);
        dueDate.set(Calendar.MINUTE, 15);
        dueDate.set(Calendar.SECOND, 0);

        //אם השעה 09:00 כבר עברה היום, נתזמן למחר ב-09:00
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        PeriodicWorkRequest notificationRequest =
                new PeriodicWorkRequest.Builder(
                        MedicationWorker.class,
                        24, TimeUnit.HOURS) //חזרה כל 24 שעות
                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MedicationDailyWork",
                ExistingPeriodicWorkPolicy.UPDATE, //מעדכן את התזמון הקיים אם יש שינוי
                notificationRequest
        );
    }

    private void checkNotificationPermission() {
        //הרשאה זו נדרשת רק עבור אנדרואיד 13 (API 33) ומעלה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                //הצגת הדיאלוג של המערכת למשתמש
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}