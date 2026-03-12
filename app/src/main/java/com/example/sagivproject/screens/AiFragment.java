package com.example.sagivproject.screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
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
 * A fragment that allows users to interact with a generative AI model.
 */
@AndroidEntryPoint
public class AiFragment extends BaseFragment {
    private ChatFutures chatSession;
    private Button send, speakBtn;
    private ProgressBar progressBar;
    private EditText questionInput;
    private TextView answerView;
    private Handler animationHandler;
    private int charIndex;
    private TextToSpeech tts;
    private boolean isSpeaking = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        send = view.findViewById(R.id.btn_Ai_send_to_Ai);
        speakBtn = view.findViewById(R.id.btn_Ai_speak);
        questionInput = view.findViewById(R.id.edit_Ai_question);
        answerView = view.findViewById(R.id.TV_Ai_txt_response);
        progressBar = view.findViewById(R.id.progressBar_Ai);

        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String id) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(true));
                    }

                    @Override
                    public void onDone(String id) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(false));
                    }

                    @Override
                    public void onError(String id) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(false));
                    }
                });
            }
        });

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(generativeModel);
        chatSession = modelFutures.startChat(Collections.emptyList());

        send.setOnClickListener(v -> sendQuestion());
        speakBtn.setOnClickListener(v -> toggleSpeech());
    }

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

    private void updateSpeakButton(boolean speaking) {
        isSpeaking = speaking;
        speakBtn.setText(speaking ? R.string.cancel_playback : R.string.playback_answer);
    }

    private void sendQuestion() {
        String q = questionInput.getText().toString().trim();
        if (q.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        send.setEnabled(false);
        answerView.setText("");
        updateSpeakButton(false);
        tts.stop();
        answerView.setText("");

        Content userMessage = new Content.Builder()
                .addText(q)
                .build();

        ListenableFuture<GenerateContentResponse> responseFuture = chatSession.sendMessage(userMessage);
        Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                progressBar.setVisibility(View.GONE);
                send.setVisibility(View.VISIBLE);
                String text = result.getText();
                if (text == null) {
                    text = "לא התקבלה תשובה.";
                }
                displayTextWithAnimation(text);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                send.setVisibility(View.VISIBLE);
                answerView.setText(String.format("שגיאה: %s", t.getMessage()));
            }
        }, mainExecutor);
    }

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
                    speakBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
