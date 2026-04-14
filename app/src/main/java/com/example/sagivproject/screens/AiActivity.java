package com.example.sagivproject.screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.ChatFutures;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity providing an AI-powered health assistant chat interface.
 * <p>
 * This activity allows users to ask health-related questions and receive responses generated
 * by Google's Gemini model via Firebase Vertex AI. Key features include:
 * <ul>
 * <li>Real-time chat interaction with generative AI.</li>
 * <li>Animated text display for AI responses (typewriter effect).</li>
 * <li>Text-to-Speech (TTS) integration to read AI responses aloud for accessibility.</li>
 * <li>Progress tracking during AI generation.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public class AiActivity extends BaseActivity {
    private ChatFutures chatSession;
    private Button btnSend, btnSpeak;
    private ProgressBar progressBar;
    private EditText questionInput;
    private TextView answerView;
    private Handler animationHandler;
    private int charIndex;
    private TextToSpeech tts;
    private boolean isSpeaking = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_ai, R.id.aiPage);
        setupMenu();

        btnSend = findViewById(R.id.btn_Ai_send_to_Ai);
        btnSpeak = findViewById(R.id.btn_Ai_speak);
        questionInput = findViewById(R.id.edit_Ai_question);
        answerView = findViewById(R.id.TV_Ai_txt_response);
        progressBar = findViewById(R.id.progressBar_Ai);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String id) {
                        runOnUiThread(() -> updateSpeakButton(true));
                    }

                    @Override
                    public void onDone(String id) {
                        runOnUiThread(() -> updateSpeakButton(false));
                    }

                    @Override
                    public void onError(String id) {
                        runOnUiThread(() -> updateSpeakButton(false));
                    }
                });
            }
        });

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(generativeModel);
        chatSession = modelFutures.startChat(Collections.emptyList());

        btnSend.setOnClickListener(v -> sendQuestion());
        btnSpeak.setOnClickListener(v -> toggleSpeech());
    }

    /**
     * Toggles playback of the AI response text using Text-to-Speech.
     */
    private void toggleSpeech() {
        if (isSpeaking) {
            tts.stop();
            updateSpeakButton(false);
        } else {
            String text = answerView.getText().toString();
            if (!text.isEmpty()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ai_res");
                updateSpeakButton(true);
            }
        }
    }

    /**
     * Updates the playback button UI state.
     *
     * @param speaking true if text is currently being read.
     */
    private void updateSpeakButton(boolean speaking) {
        isSpeaking = speaking;
        btnSpeak.setText(speaking ? R.string.cancel_playback : R.string.playback_answer);
    }

    /**
     * Sends the user's question to the AI model and manages the asynchronous response.
     */
    private void sendQuestion() {
        String q = questionInput.getText().toString().trim();
        if (q.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);
        answerView.setText("");
        updateSpeakButton(false);
        tts.stop();
        answerView.setText("");

        Content userMessage = new Content.Builder()
                .addText(q)
                .build();

        ListenableFuture<GenerateContentResponse> responseFuture = chatSession.sendMessage(userMessage);
        Executor mainExecutor = ContextCompat.getMainExecutor(this);

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);
                String text = result.getText();
                if (text == null) {
                    text = "לא התקבלה תשובה.";
                }
                displayTextWithAnimation(text);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);
                answerView.setText(String.format("שגיאה: %s", t.getMessage()));
            }
        }, mainExecutor);
    }

    /**
     * Animates the display of AI response text character by character.
     *
     * @param fullText The full response string to display.
     */
    private void displayTextWithAnimation(String fullText) {
        if (fullText == null) return;
        charIndex = 0;
        animationHandler = new Handler(Looper.getMainLooper());
        final int delay = 15;

        animationHandler.post(new Runnable() {
            @Override
            public void run() {
                if (charIndex < fullText.length()) {
                    answerView.append(String.valueOf(fullText.charAt(charIndex++)));
                    animationHandler.postDelayed(this, delay);
                } else {
                    btnSpeak.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Shuts down the TTS engine and clears animation callbacks when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        if (tts != null) tts.shutdown();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}