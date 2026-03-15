package com.example.sagivproject.screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment that displays developer and contact information.
 * <p>
 * This fragment also contains a hidden navigation trigger (long press) to access
 * a secret area of the application.
 * </p>
 */
@AndroidEntryPoint
public class ContactFragment extends BaseFragment {
    private static final int LONG_PRESS_DURATION = 4000;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable longPressedRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imgContactIcon = view.findViewById(R.id.imgContactIcon);
        setupSecretNavigation(imgContactIcon);
    }

    /**
     * Sets up a long press listener on a view to navigate to a secret screen.
     *
     * @param view The view to attach the secret navigation logic to.
     */
    private void setupSecretNavigation(View view) {
        longPressedRunnable = () -> navigateTo(R.id.action_contactFragment_to_secretFragment);

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
