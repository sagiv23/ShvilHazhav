package com.example.sagivproject.screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.sagivproject.services.ITTSService;
import com.example.sagivproject.services.ITTSService.TTSListener;
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
import java.util.concurrent.Executor;

import javax.inject.Inject;

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
    private static final String PREF_AI_QUESTION = "ai_question_text";
    private static final String PREF_AI_ANSWER = "ai_answer_text";
    private static final String PREF_AI_SPEAK_VISIBILITY = "ai_speak_visibility";
    /**
     * Singleton service for Text-to-Speech functionality.
     */
    @Inject
    protected ITTSService ttsService;
    /**
     * Google Gemini AI chat session.
     */
    private ChatFutures chatSession;

    private Button btnSend, btnSpeak;
    private ProgressBar progressBar;
    private EditText questionInput;
    private TextView answerView;

    /**
     * Handler for managing the character-by-character typewriter animation.
     */
    private Handler animationHandler;

    /**
     * Current character index in the typewriter animation.
     */
    private int charIndex;

    /**
     * Buffer containing the full AI response text.
     */
    private String currentFullResponse = "";

    /**
     * Flag indicating if the response is currently being read aloud.
     */
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
        animationHandler = new Handler(Looper.getMainLooper());

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(generativeModel);
        chatSession = modelFutures.startChat(Collections.emptyList());

        btnSend.setOnClickListener(v -> sendQuestion());
        btnSpeak.setOnClickListener(v -> toggleSpeech());

        // Load from savedInstanceState (for config changes)
        if (savedInstanceState != null) {
            String savedAnswer = savedInstanceState.getString("answerText");
            currentFullResponse = savedAnswer != null ? savedAnswer : "";
            if (savedAnswer != null && !savedAnswer.isEmpty()) {
                answerView.setText(savedAnswer);
                btnSpeak.setVisibility(savedInstanceState.getInt("speakBtnVisibility", View.GONE));
            }
            questionInput.setText(savedInstanceState.getString("questionText", ""));
        } else {
            // Load from SharedPreferences (for returning to activity)
            String savedQuestion = sharedPreferencesUtil.getString(PREF_AI_QUESTION, "");
            String savedAnswer = sharedPreferencesUtil.getString(PREF_AI_ANSWER, "");
            int speakVisibility = sharedPreferencesUtil.getInt(PREF_AI_SPEAK_VISIBILITY, View.GONE);

            currentFullResponse = savedAnswer;
            questionInput.setText(savedQuestion);
            answerView.setText(savedAnswer);
            btnSpeak.setVisibility(speakVisibility);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("answerText", answerView.getText().toString());
        outState.putString("questionText", questionInput.getText().toString());
        outState.putInt("speakBtnVisibility", btnSpeak.getVisibility());
    }

    /**
     * Toggles playback of the AI response text using Text-to-Speech.
     */
    private void toggleSpeech() {
        if (isSpeaking) {
            ttsService.stop();
            updateSpeakButton(false);
        } else {
            String text = answerView.getText().toString();
            if (!text.isEmpty()) {
                ttsService.speak(text, "ai_res", new TTSListener() {
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
        ttsService.stop();
        answerView.setText("");
        currentFullResponse = "";

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

                // Add a subtle bounce animation to the card containing the answer
                View card = findViewById(R.id.card_ai_container);
                if (card != null) {
                    card.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200)
                            .withEndAction(() -> card.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                            .start();
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);
                String errorMsg = String.format("שגיאה: %s", t.getMessage());
                answerView.setText(errorMsg);
                currentFullResponse = errorMsg;
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
        currentFullResponse = fullText;
        charIndex = 0;
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

    @Override
    protected void onPause() {
        super.onPause();
        ttsService.stop();
        updateSpeakButton(false);
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }

        // Save state to SharedPreferences
        sharedPreferencesUtil.saveString(PREF_AI_QUESTION, questionInput.getText().toString());
        // Save the full response even if it was still animating
        sharedPreferencesUtil.saveString(PREF_AI_ANSWER, currentFullResponse);
        sharedPreferencesUtil.saveInt(PREF_AI_SPEAK_VISIBILITY, btnSpeak.getVisibility());
    }

    /**
     * Shuts down the TTS engine and clears animation callbacks when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        if (ttsService != null) {
            ttsService.stop();
        }
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}