package com.example.sagivproject.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
import com.google.android.material.button.MaterialButton;

/**
 * A fragment for the secret page.
 */
public class SecretFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_secret, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSocialButton(view, R.id.btnYoutube, "https://www.youtube.com/@Sagiv23");
        setupSocialButton(view, R.id.btnInstagram, "https://www.instagram.com/Sagiv23");
        setupSocialButton(view, R.id.btnGithub, "https://github.com/sagiv23");
    }

    private void setupSocialButton(View view, int buttonId, String url) {
        MaterialButton button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
}
