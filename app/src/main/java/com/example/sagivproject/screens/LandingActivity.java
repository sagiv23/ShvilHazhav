package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The initial landing activity for unauthenticated users.
 * <p>
 * This activity serves as the entry point when no user is logged in. It provides options
 * to log in, register, or view contact information. It features an entry animation
 * (fade and scale) if triggered from the splash screen.
 * </p>
 */
@AndroidEntryPoint
public class LandingActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_landing, R.id.landingPage);
        setupMenu();

        LinearLayout mainContent = findViewById(R.id.mainContent);
        ImageView splashImage = findViewById(R.id.splashImage);

        boolean shouldAnimate = getIntent().getBooleanExtra("shouldAnimate", false);

        if (shouldAnimate && savedInstanceState == null) {
            if (mainContent != null) {
                mainContent.setAlpha(0f);
            }

            if (splashImage != null) {
                splashImage.setVisibility(View.VISIBLE);
                splashImage.setAlpha(1f);
                splashImage.setScaleX(1f);
                splashImage.setScaleY(1f);

                splashImage.animate()
                        .scaleX(0.5f)
                        .scaleY(0.5f)
                        .alpha(0f)
                        .setDuration(1500)
                        .setStartDelay(500)
                        .withEndAction(() -> {
                            splashImage.setVisibility(View.GONE);
                            if (mainContent != null) {
                                mainContent.animate()
                                        .alpha(1f)
                                        .setDuration(600)
                                        .start();
                            }
                        })
                        .start();
            }
        } else {

            if (splashImage != null) {
                splashImage.setVisibility(View.GONE);
            }
            if (mainContent != null) {
                mainContent.setAlpha(1f);
            }
        }

        findViewById(R.id.btn_landingBody_to_contact).setOnClickListener(v -> startActivity(new Intent(this, ContactActivity.class)));
        findViewById(R.id.btn_landingBody_to_login).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.btn_landingBody_to_register).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}