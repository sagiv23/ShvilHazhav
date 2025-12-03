package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.BuildConfig;
import com.example.sagivproject.R;
import com.example.sagivproject.utils.EncryptionAPIKey;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiActivity extends AppCompatActivity {
    Button btnToMain, btnToContact, btnToDetailsAboutUser, send, btnToExit;
    private ProgressBar progressBar;
    private EditText questionInput;
    private TextView answerView;

    private static final String API_KEY = EncryptionAPIKey.decode(BuildConfig.API_KEY);
    private static final String MODEL = "models/gemini-2.5-flash";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/" + MODEL + ":generateContent?key=" + API_KEY;

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.aiPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkUserPage(this);

        btnToMain = findViewById(R.id.btn_Ai_to_main);
        btnToContact = findViewById(R.id.btn_Ai_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_Ai_to_DetailsAboutUser);
        send = findViewById(R.id.btn_Ai_send_to_Ai);
        questionInput = findViewById(R.id.edit_Ai_question);
        answerView = findViewById(R.id.TV_Ai_txt_response);
        progressBar = findViewById(R.id.progressBar_Ai);
        btnToExit = findViewById(R.id.btn_Ai_to_exit);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(AiActivity.this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(AiActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(AiActivity.this, DetailsAboutUserActivity.class)));
        send.setOnClickListener(view -> sendQuestion());
        btnToExit.setOnClickListener(view ->  LogoutHelper.logout(AiActivity.this));
    }

    private void displayTextWithAnimation(TextView textView, String fullText) {
        textView.setText("");
        final int delay = 15; //מהירות כתיבה במילישניות לכל תו

        new Thread(() -> {
            StringBuilder displayedText = new StringBuilder();

            for (int i = 0; i < fullText.length(); i++) {
                displayedText.append(fullText.charAt(i));
                String current = displayedText.toString();

                runOnUiThread(() -> textView.setText(current));

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendQuestion() {
        String q = questionInput.getText().toString().trim();
        if (q.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        answerView.setText("");

        try {
            JSONObject textPart = new JSONObject().put("text", q);
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
                        progressBar.setVisibility(View.GONE);
                        answerView.setText("שגיאה: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String r = response.body().string();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (!response.isSuccessful()) {
                            answerView.setText("קוד שגיאה: " + response.code() + "\n" + r);
                            return;
                        }
                        try {
                            JSONObject obj = new JSONObject(r);
                            String text = obj
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            displayTextWithAnimation(answerView, text);
                        } catch (Exception e) {
                            answerView.setText("פירוק תשובה נכשל: " + e.getMessage());
                        }
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            answerView.setText("שגיאה: " + e.getMessage());
        }
    }
}