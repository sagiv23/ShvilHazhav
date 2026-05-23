package com.example.sagivproject.services;

/**
 * Service for handling Text-to-Speech (TTS) operations across the application.
 * <p>
 * This interface provides a simplified facade for Android's TextToSpeech engine,
 * allowing activities to trigger speech without managing the engine's lifecycle,
 * initialization, or locale settings.
 * </p>
 */
public interface ITTSService {
    /**
     * Converts the provided text to speech.
     * <p>
     * The service automatically handles engine initialization if it's the first call.
     * It uses Hebrew (IL) as the default locale for this application.
     * </p>
     *
     * @param text     The string content to be read aloud.
     * @param id       A unique identifier for this specific speech request, used in listeners.
     * @param listener An optional callback listener to receive progress events (start, done, error).
     */
    void speak(String text, String id, TTSListener listener);

    /**
     * Immediately stops any ongoing speech playback and clears the queue.
     */
    void stop();

    /**
     * Callback interface for receiving updates about the progress of a specific speech utterance.
     */
    interface TTSListener {
        /**
         * Invoked when the engine begins speaking the text associated with the given ID.
         *
         * @param id The unique identifier of the utterance.
         */
        void onStart(String id);

        /**
         * Invoked when the engine successfully finishes speaking the text.
         *
         * @param id The unique identifier of the utterance.
         */
        void onDone(String id);

        /**
         * Invoked if an error occurs during the processing or playback of the text.
         *
         * @param id The unique identifier of the utterance.
         */
        void onError(String id);
    }
}