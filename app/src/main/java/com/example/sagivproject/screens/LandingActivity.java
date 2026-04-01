package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.landingPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        LinearLayout mainContent = findViewById(R.id.mainContent);
        ImageView splashImage = findViewById(R.id.splashImage);

        boolean shouldAnimate = getIntent().getBooleanExtra("shouldAnimate", false);

        if (shouldAnimate) {
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

        Button btnToContact = findViewById(R.id.btn_landingBody_to_contact);
        Button btnToLogin = findViewById(R.id.btn_landingBody_to_login);
        Button btnToRegister = findViewById(R.id.btn_landingBody_to_register);

        btnToContact.setOnClickListener(v -> startActivity(new Intent(this, ContactActivity.class)));
        btnToLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        btnToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}