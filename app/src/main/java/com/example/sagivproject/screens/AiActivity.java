package com.example.sagivproject.screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

/**
 * An activity that allows users to interact with a generative AI model.
 * Users can ask questions and receive answers from the AI.
 * Includes Text-to-Speech capability to hear the AI's response.
 */
public class AiActivity extends BaseActivity {
    private ChatFutures chatSession;
    private Button send;
    private Button speakBtn;
    private ProgressBar progressBar;
    private EditText questionInput;
    private TextView answerView;
    private Handler animationHandler;
    private int charIndex;
    private TextToSpeech tts;

    /**
     * Initializes the activity, its views, and the AI chat session.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        send = findViewById(R.id.btn_Ai_send_to_Ai);
        speakBtn = findViewById(R.id.btn_Ai_speak);
        questionInput = findViewById(R.id.edit_Ai_question);
        answerView = findViewById(R.id.TV_Ai_txt_response);
        progressBar = findViewById(R.id.progressBar_Ai);

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("he", "IL"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "שפה לא נתמכת ב-TTS", Toast.LENGTH_SHORT).show();
                }
            }
        });

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(generativeModel);
        chatSession = modelFutures.startChat(Collections.emptyList());

        send.setOnClickListener(view -> sendQuestion());
        speakBtn.setOnClickListener(view -> speakResponse());
    }

    /**
     * Sends the user's question to the generative AI model and displays the response.
     * It handles the UI state during the request, showing a progress bar and disabling the send button.
     */
    private void sendQuestion() {
        String q = questionInput.getText().toString().trim();
        if (q.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        send.setVisibility(View.GONE);
        speakBtn.setVisibility(View.GONE); // Hide speak button for new question
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
                send.setVisibility(View.VISIBLE);
                String text = result.getText();
                if (text == null) {
                    text = "לא התקבלה תשובה.";
                }
                displayTextWithAnimation(answerView, text);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                send.setVisibility(View.VISIBLE);
                answerView.setText(String.format("שגיאה: %s", t.getMessage()));
            }
        }, mainExecutor);
    }

    /**
     * Displays the given text in the TextView with a character-by-character animation.
     *
     * @param textView The TextView to display the text in.
     * @param fullText The full text to be displayed.
     */
    private void displayTextWithAnimation(TextView textView, String fullText) {
        textView.setText("");
        charIndex = 0;
        animationHandler = new Handler(Looper.getMainLooper());
        final int delay = 15;

        animationHandler.post(new Runnable() {
            @Override
            public void run() {
                if (charIndex < fullText.length()) {
                    textView.append(String.valueOf(fullText.charAt(charIndex++)));
                    animationHandler.postDelayed(this, delay);
                } else {
                    // Animation complete, show the speak button
                    speakBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void speakResponse() {
        String text = answerView.getText().toString();
        if (!text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
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
