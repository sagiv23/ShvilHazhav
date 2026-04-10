package com.example.sagivproject.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that displays developer information and contact links.
 * <p>
 * This screen provides users with information about the application's developer and
 * includes links to social media and professional profiles. It also features a
 * "hidden" navigation trigger (Easter egg) activated by a long press on the contact icon.
 * </p>
 */
@AndroidEntryPoint
public class ContactActivity extends BaseActivity {
    /**
     * The duration required for a long press to trigger the secret navigation, in milliseconds.
     */
    private static final int LONG_PRESS_DURATION = 3000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable longPressedRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_contact, R.id.contactPage);
        setupMenu();

        setupSecretNavigation(findViewById(R.id.imgContactIcon));

        setupSocialButton(R.id.btnYoutube, "https://www.youtube.com/@Sagiv23");
        setupSocialButton(R.id.btnInstagram, "https://www.instagram.com/Sagiv23");
        setupSocialButton(R.id.btnGithub, "https://github.com/sagiv23");
    }

    /**
     * Configures a view to respond to a long press by revealing a secret UI component.
     *
     * @param view The view to attach the secret navigation logic to.
     */
    private void setupSecretNavigation(View view) {
        View cardSecret = findViewById(R.id.cardSecret);

        longPressedRunnable = () -> {
            if (cardSecret.getVisibility() != View.VISIBLE) {
                cardSecret.setAlpha(0f);
                cardSecret.setVisibility(View.VISIBLE);
                cardSecret.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .start();
            }
        };

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.postDelayed(longPressedRunnable, LONG_PRESS_DURATION);
                    return true;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    handler.removeCallbacks(longPressedRunnable);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(longPressedRunnable);
                    return true;
            }
            return false;
        });
    }

    /**
     * Helper to configure a button to open an external URL when clicked.
     *
     * @param buttonId The resource ID of the MaterialButton.
     * @param url      The web address to navigate to.
     */
    private void setupSocialButton(int buttonId, String url) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
}