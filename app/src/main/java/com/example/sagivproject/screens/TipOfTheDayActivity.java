package com.example.sagivproject.screens;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
 * Activity that displays a daily motivational tip and an inspirational quote.
 * <p>
 * This screen performs a daily content check:
 * <ul>
 * <li>It attempts to fetch today's tip from the shared database.</li>
 * <li>If no tip exists for today, it uses the Google Gemini AI model to generate a unique, relevant tip.</li>
 * <li>Generated tips are saved back to the database for all other users to see.</li>
 * <li>It provides Text-to-Speech (TTS) support for both the daily tip and the quote.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public class TipOfTheDayActivity extends BaseActivity {
    /** Static list of fallback inspirational quotes. */
    private final String[] inspirationalQuotes = {
            "ההצלחה היא סך הכל של מאמצים קטנים, שחוזרים עליהם יום יום.",
            "הדרך הטובה ביותר לחזות את העתיד היא ליצור אותו.",
            "אל תחכה. הזמן לעולם לא יהיה בדיוק מתאים.",
            "האמינו בעצמכם וכל מה שאתם. דעו שיש בכם משהו גדול יותר מכל מכשול.",
            "ההתחלה היא החלק החשוב ביותר בעבודה."
    };

    private TextView tipContent, tvInspirationContent;
    private Button btnTipSpeak, btnInspirationSpeak;
    private GenerativeModelFutures model;
    private TextToSpeech tts;
    /** Tracks which content ID is currently being played via TTS. */
    private String currentlySpeakingId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tip_of_the_day);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tipOfTheDayPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        ((TextView) findViewById(R.id.tv_tip_of_the_day_date)).setText(currentDate);

        tipContent = findViewById(R.id.tv_tipOfTheDay_content);
        tvInspirationContent = findViewById(R.id.tv_tipOfTheDay_inspiration_content);
        btnTipSpeak = findViewById(R.id.btn_tip_speak);
        btnInspirationSpeak = findViewById(R.id.btn_inspiration_speak);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String id) { runOnUiThread(() -> updateSpeakButton(id, true)); }

                    @Override
                    public void onDone(String id) { runOnUiThread(() -> updateSpeakButton(id, false)); }

                    @Override
                    public void onError(String id) { runOnUiThread(() -> updateSpeakButton(id, false)); }
                });
            }
        });

        tvInspirationContent.setText(inspirationalQuotes[new Random().nextInt(inspirationalQuotes.length)]);
        btnTipSpeak.setOnClickListener(v -> toggleSpeech("tip", tipContent.getText().toString()));
        btnInspirationSpeak.setOnClickListener(v -> toggleSpeech("inspiration", tvInspirationContent.getText().toString()));

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        model = GenerativeModelFutures.from(generativeModel);

        checkDailyTip();
    }

    /**
     * Toggles the audio playback for a specific text block.
     * @param id Unique ID for the content piece.
     * @param text The actual text to speak.
     */
    private void toggleSpeech(String id, String text) {
        if (id.equals(currentlySpeakingId)) {
            tts.stop();
            updateSpeakButton(id, false);
        } else {
            if (currentlySpeakingId != null) tts.stop();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id);
            updateSpeakButton(id, true);
        }
    }

    /**
     * Updates the UI state of the relevant playback button.
     * @param id The content ID.
     * @param speaking true if currently playing.
     */
    private void updateSpeakButton(String id, boolean speaking) {
        Button btn = id.equals("tip") ? btnTipSpeak : btnInspirationSpeak;
        currentlySpeakingId = speaking ? id : null;
        btn.setText(speaking ? R.string.cancel_playback : R.string.playback);
    }

    /** Checks Firebase for an existing tip for today. Triggers AI generation if missing. */
    private void checkDailyTip() {
        databaseService.getTipOfTheDayService().getTipForToday(new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(TipOfTheDay result) {
                if (result != null) {
                    tipContent.setText(result.getTip());
                    btnTipSpeak.setVisibility(View.VISIBLE);
                } else fetchDailyTipFromAI();
            }

            @Override
            public void onFailed(Exception e) { tipContent.setText("שגיאה בקבלת הטיפ היומי."); }
        });
    }

    /** Uses Vertex AI to generate a localized tip and persists it to the database. */
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
                if (text == null) text = "לא התקבלה תשובה.";

                String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                TipOfTheDay newTip = new TipOfTheDay(text, today);
                newTip.setId(today);

                String finalText = text;
                databaseService.getTipOfTheDayService().saveTipIfNotExists(newTip, new IDatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(TipOfTheDay finalResult) {
                        String tipToDisplay = (finalResult != null) ? finalResult.getTip() : finalText;
                        tipContent.setAlpha(0f);
                        tipContent.setText(tipToDisplay);
                        tipContent.animate().alpha(1f).setDuration(800).withEndAction(() -> btnTipSpeak.setVisibility(View.VISIBLE));
                    }

                    @Override
                    public void onFailed(Exception e) { tipContent.setText("שגיאה בשמירת הטיפ היומי."); }
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) { tipContent.setText(String.format("שגיאה: %s", t.getMessage())); }
        }, mainExecutor);
    }

    /** Releases TTS resources on activity close. */
    @Override
    public void onDestroy() {
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}