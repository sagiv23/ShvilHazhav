package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.BuildConfig;
import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.EncryptionAPIKey;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SystemFunctionsActivity extends BaseActivity {
    private Button btnToAdminPage, btnCheckAi, btnCheckMedications, btnCheckGame;
    private TextView txtDescription;

    private static final String API_KEY = EncryptionAPIKey.decode(BuildConfig.API_KEY);
    private static final String MODEL = "models/gemini-2.5-flash";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/" + MODEL + ":generateContent?key=" + API_KEY;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_system_functions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.systemFunctionsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToAdminPage = findViewById(R.id.btn_SystemFunctionsPage_to_admin);
        txtDescription = findViewById(R.id.txt_SystemFunctionsPage_description);
        btnCheckAi = findViewById(R.id.btn_SystemFunctionsPage_ai);
        btnCheckMedications = findViewById(R.id.btn_SystemFunctionsPage_medicationList);
        btnCheckGame = findViewById(R.id.btn_SystemFunctionsPage_memoryGame);

        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(SystemFunctionsActivity.this, AdminPageActivity.class)));
        btnCheckAi.setOnClickListener(v -> { checkAi(); });
        btnCheckMedications.setOnClickListener(v -> { checkMedications(); });
        btnCheckGame.setOnClickListener(v -> { checkGame(); });
    }

    private void showSystemErrors(String text) {
        runOnUiThread(() -> {
            txtDescription.setText(text);
        });
    }

    private void checkAi() {
        Log.d("SystemFunctionsActivity", "Checking AI Advisor...");
        Toast.makeText(this, "בודק את יועץ ה-AI...", Toast.LENGTH_SHORT).show();

        try {
            JSONObject textPart = new JSONObject().put("text", "Hello");
            JSONArray parts = new JSONArray().put(textPart);
            JSONObject content = new JSONObject().put("parts", parts);
            JSONArray contents = new JSONArray().put(content);
            JSONObject json = new JSONObject().put("contents", contents);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("x-goog-api-key", API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Log.e("SystemFunctionsActivity", "AI Check failed", e);
                        showSystemErrors(
                                "שגיאה ביועץ AI:\n" +
                                        "• כשל בתקשורת עם השרת\n" +
                                        "• מפתח API לא תקין או תגובת שרת שגויה"
                        );
                        Toast.makeText(SystemFunctionsActivity.this, "בדיקת AI נכשלה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String r = response.body().string();
                    runOnUiThread(() -> {
                        if (!response.isSuccessful()) {
                            Log.e("SystemFunctionsActivity", "AI Check failed with code: " + response.code() + " " + r);
                            showSystemErrors(
                                    "שגיאה ביועץ AI:\n" +
                                            "• כשל בתקשורת עם השרת\n" +
                                            "• מפתח API לא תקין או תגובת שרת שגויה"
                            );
                            Toast.makeText(SystemFunctionsActivity.this, "בדיקת AI נכשלה: " + response.code(), Toast.LENGTH_LONG).show();
                        } else {
                            Log.d("SystemFunctionsActivity", "AI Check successful");
                            showSystemErrors("בדיקת יועץ ה-AI עברה בהצלחה ללא שגיאות");
                            Toast.makeText(SystemFunctionsActivity.this, "בדיקת יועץ ה-AI עברה בהצלחה ללא שגיאות", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e("SystemFunctionsActivity", "AI Check failed", e);
            Toast.makeText(SystemFunctionsActivity.this, "בדיקת AI נכשלה: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkMedications() {
        Log.d("SystemFunctionsActivity", "Checking Medications...");
        Toast.makeText(this, "בודק את התרופות...", Toast.LENGTH_SHORT).show();

        List<String> errors = new ArrayList<>();

        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> userList) {
                if (userList == null || userList.isEmpty()) {
                    Toast.makeText(SystemFunctionsActivity.this, "לא נמצאו משתמשים", Toast.LENGTH_SHORT).show();
                    return;
                }

                AtomicInteger usersProcessed = new AtomicInteger(0);
                for (User user : userList) {
                    databaseService.getUserMedicationList(user.getUid(), new DatabaseService.DatabaseCallback<List<Medication>>() {
                        @Override
                        public void onCompleted(List<Medication> medicationList) {
                            if (medicationList != null) {
                                for (Medication med : medicationList) {
                                    String errorMsg;
                                    if (med.getId() == null || med.getId().isEmpty()) {
                                        errorMsg = "User " + user.getFullName() + ": Medication with no ID";
                                        errors.add(errorMsg);
                                        Log.e("SystemFunctionsActivity", errorMsg);
                                    }
                                    if (med.getName() == null || med.getName().isEmpty()) {
                                        errorMsg = "User " + user.getFullName() + ": Medication (" + (med.getId() != null ? med.getId() : "N/A") + ") with no name";
                                        errors.add(errorMsg);
                                        Log.e("SystemFunctionsActivity", errorMsg);
                                    }
                                    if (med.getDateTimestamp() == 0) {
                                        errorMsg = "User " + user.getFullName() + ": Medication (" + (med.getId() != null ? med.getId() : "N/A") + ") with no date";
                                        errors.add(errorMsg);
                                        Log.e("SystemFunctionsActivity", errorMsg);
                                    }
                                }
                            }
                            if (usersProcessed.incrementAndGet() == userList.size()) {
                                runOnUiThread(() -> {
                                    if (errors.isEmpty()) {
                                        showSystemErrors(
                                                "בדיקת רשימת התרופות הסתיימה ללא שגיאות"
                                        );
                                        Toast.makeText(SystemFunctionsActivity.this, "בדיקת התרופות הסתיימה ללא שגיאות", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorString = "נמצאו שגיאות ברשימת התרופות:\n\n• " + String.join("\n• ", errors);
                                        showSystemErrors(errorString);
                                        Toast.makeText(SystemFunctionsActivity.this, "נמצאו שגיאות:\n" + errorString, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {
                            String errorMsg = "Failed to get meds for user: " + user.getFullName();
                            Log.e("SystemFunctionsActivity", errorMsg, e);
                            errors.add(errorMsg);
                            if (usersProcessed.incrementAndGet() == userList.size()) {
                                runOnUiThread(() -> {
                                    String errorString = "נמצאו שגיאות ברשימת התרופות:\n\n• " + String.join("\n• ", errors);
                                    showSystemErrors(errorString);
                                    Toast.makeText(SystemFunctionsActivity.this, "נמצאו שגיאות:\n" + errorString, Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("SystemFunctionsActivity", "Failed to get user list", e);
                Toast.makeText(SystemFunctionsActivity.this, "שגיאה בקבלת רשימת המשתמשים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkGame() {
        Log.d("SystemFunctionsActivity", "Checking Game...");
        showSystemErrors(
                "בדיקת משחק הזיכרון טרם מומשה\n" +
                        "ייתכן וקיימות תקלות שאינן מזוהות כרגע"
        );
        // TODO: Implement game check
        Toast.makeText(this, "בודק את המשחקים...", Toast.LENGTH_SHORT).show();
    }
}
