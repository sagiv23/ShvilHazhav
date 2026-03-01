package com.example.sagivproject.screens;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.services.IDatabaseService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity that displays a "Tip of the Day" and an inspirational quote.
 * The tip is fetched from the database or a generative AI model and stored for the day.
 */
@AndroidEntryPoint
public class TipOfTheDayActivity extends BaseActivity {
    private final String[] inspirationalQuotes = {
            "ההצלחה היא סך הכל של מאמצים קטנים, שחוזרים עליהם יום יום.",
            "הדרך הטובה ביותר לחזות את העתיד היא ליצור אותו.",
            "אל תחכה. הזמן לעולם לא יהיה בדיוק מתאים.",
            "האמינו בעצמכם וכל מה שאתם. דעו שיש בכם משהו גדול יותר מכל מכשול.",
            "ההתחלה היא החלק החשוב ביותר בעבודה."
    };
    private TextView tipContent;
    private TextView tvInspirationContent;
    private Button btnTipSpeak;
    private Button btnInspirationSpeak;
    private GenerativeModelFutures model;
    private TextToSpeech tts;
    private String currentlySpeakingId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_of_the_day);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tipOfTheDayPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        TextView tvDate = findViewById(R.id.tv_tip_of_the_day_date);
        tvDate.setText(currentDate);

        tipContent = findViewById(R.id.tv_tipOfTheDay_content);
        tvInspirationContent = findViewById(R.id.tv_tipOfTheDay_inspiration_content);
        btnTipSpeak = findViewById(R.id.btn_tip_speak);
        btnInspirationSpeak = findViewById(R.id.btn_inspiration_speak);

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("he", "IL"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "שפה לא נתמכת ב-TTS", Toast.LENGTH_SHORT).show();
                }

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> updateSpeakButton(utteranceId, true));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> updateSpeakButton(utteranceId, false));
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> updateSpeakButton(utteranceId, false));
                    }
                });
            }
        });

        tipContent.setAlpha(0f);
        tipContent.animate().alpha(1f).setDuration(800).withEndAction(() -> btnTipSpeak.setVisibility(View.VISIBLE));

        Random random = new Random();
        int index = random.nextInt(inspirationalQuotes.length);
        tvInspirationContent.setText(inspirationalQuotes[index]);

        btnTipSpeak.setOnClickListener(v -> toggleSpeech("tip", tipContent.getText().toString()));
        btnInspirationSpeak.setOnClickListener(v -> toggleSpeech("inspiration", tvInspirationContent.getText().toString()));

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        model = GenerativeModelFutures.from(generativeModel);

        checkDailyTip();
    }

    private void toggleSpeech(String id, String text) {
        if (id.equals(currentlySpeakingId)) {
            tts.stop();
            updateSpeakButton(id, false);
        } else {
            if (currentlySpeakingId != null) {
                tts.stop();
                updateSpeakButton(currentlySpeakingId, false);
            }
            if (text != null && !text.isEmpty()) {
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id);
                updateSpeakButton(id, true);
            }
        }
    }

    private void updateSpeakButton(String id, boolean speaking) {
        Button btn = id.equals("tip") ? btnTipSpeak : btnInspirationSpeak;
        if (speaking) {
            currentlySpeakingId = id;
            btn.setText(R.string.cancel_playback);
        } else {
            currentlySpeakingId = null;
            btn.setText(R.string.playback);
        }
    }

    private void checkDailyTip() {
        databaseService.getTipOfTheDayService().getTipForToday(new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(TipOfTheDay result) {
                if (result != null) {
                    tipContent.setText(result.getTip());
                    btnTipSpeak.setVisibility(View.VISIBLE);
                } else {
                    fetchDailyTipFromAI();
                }
            }

            @Override
            public void onFailed(Exception e) {
                tipContent.setText("שגיאה בקבלת הטיפ היומי.");
            }
        });
    }

    private void fetchDailyTipFromAI() {
        tipContent.setText("טוען טיפ יומי...");
        btnTipSpeak.setVisibility(View.GONE);

        String prompt = "תן טיפ יומי קצר לחיים בעברית. עד 6 משפטים. בלי אימוג'ים. בבקשה תשלח רק את התשובה בלי הקדמה מיותרת";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);
        Executor mainExecutor = ContextCompat.getMainExecutor(this);

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text == null) {
                    text = "לא התקבלה תשובה.";
                }

                String finalText = text;
                databaseService.getTipOfTheDayService().saveTipForToday(finalText, new IDatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void result) {
                        tipContent.setAlpha(0f);
                        tipContent.setText(finalText);
                        tipContent.animate().alpha(1f).setDuration(800).withEndAction(() -> btnTipSpeak.setVisibility(View.VISIBLE));
                    }

                    @Override
                    public void onFailed(Exception e) {
                        tipContent.setText("שגיאה בשמירת הטיפ היומי.");
                    }
                });

            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                tipContent.setText(String.format("שגיאה: %s", t.getMessage()));
            }
        }, mainExecutor);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
