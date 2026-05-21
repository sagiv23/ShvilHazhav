package com.example.sagivproject.services.impl;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.example.sagivproject.services.ITTSService;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Implementation of {@link ITTSService} using Android's {@link TextToSpeech}.
 * <p>
 * This class is a Singleton that manages a single {@link TextToSpeech} instance for the entire app.
 * It implements lazy initialization, meaning the engine is only created when the first
 * speech request is made.
 * </p>
 */
@Singleton
public class TTSServiceImpl implements ITTSService {
    /**
     * The application context used to initialize the TTS engine.
     */
    private final Context context;
    /**
     * The underlying Android TTS engine.
     */
    private TextToSpeech tts;
    /**
     * Flag indicating if the engine has been successfully initialized and the language is set.
     */
    private boolean isInitialized = false;

    /**
     * Constructs a new TTSServiceImpl.
     *
     * @param context The application context, injected by Hilt.
     */
    @Inject
    public TTSServiceImpl(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Internal helper to ensure the TTS engine is ready before performing an action.
     *
     * @param onReady A runnable to execute once the engine is successfully initialized.
     */
    private void ensureInitialized(Runnable onReady) {
        if (isInitialized && tts != null) {
            onReady.run();
            return;
        }

        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
                isInitialized = true;
                onReady.run();
            }
        });
    }

    @Override
    public void speak(String text, String id, TTSListener listener) {
        ensureInitialized(() -> {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    if (listener != null) listener.onStart(utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {
                    if (listener != null) listener.onDone(utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    if (listener != null) listener.onError(utteranceId);
                }
            });

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id);
        });
    }

    @Override
    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }
}
