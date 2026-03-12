package com.example.sagivproject.screens;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
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
 * A fragment that displays a "Tip of the Day".
 */
@AndroidEntryPoint
public class TipOfTheDayFragment extends BaseFragment {
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
    private String currentlySpeakingId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tip_of_the_day, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        ((TextView) view.findViewById(R.id.tv_tip_of_the_day_date)).setText(currentDate);

        tipContent = view.findViewById(R.id.tv_tipOfTheDay_content);
        tvInspirationContent = view.findViewById(R.id.tv_tipOfTheDay_inspiration_content);
        btnTipSpeak = view.findViewById(R.id.btn_tip_speak);
        btnInspirationSpeak = view.findViewById(R.id.btn_inspiration_speak);

        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String id) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(id, true));
                    }

                    @Override
                    public void onDone(String id) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(id, false));
                    }

                    @Override
                    public void onError(String id) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(id, false));
                    }
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

    private void updateSpeakButton(String id, boolean speaking) {
        Button btn = id.equals("tip") ? btnTipSpeak : btnInspirationSpeak;
        currentlySpeakingId = speaking ? id : null;
        btn.setText(speaking ? R.string.cancel_playback : R.string.playback);
    }

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
        Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text == null) {
                    text = "לא התקבלה תשובה.";
                }

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
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
