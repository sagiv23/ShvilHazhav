package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;

/**
 * A simple activity to display contact information.
 * <p>
 * This screen shows static contact details and includes the standard top menu.
 * </p>
 */
public class ContactActivity extends BaseActivity {
    private static final int LONG_PRESS_DURATION = 4000;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable longPressedRunnable;

    /**
     * Initializes the activity, sets up the UI, and configures the top menu.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contactPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        ImageView imgContactIcon = findViewById(R.id.imgContactIcon);
        setupSecretNavigation(imgContactIcon);
    }

    private void setupSecretNavigation(View view) {
        longPressedRunnable = () -> {
            Intent intent = new Intent(ContactActivity.this, SecretActivity.class);
            startActivity(intent);
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
}
