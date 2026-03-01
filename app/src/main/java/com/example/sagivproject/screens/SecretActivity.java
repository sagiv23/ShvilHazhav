package com.example.sagivproject.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.google.android.material.button.MaterialButton;

public class SecretActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_secret);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.secretPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_secret_back).setOnClickListener(v -> finish());

        setupSocialButton(R.id.btnYoutube, "https://www.youtube.com/@Sagiv23");
        setupSocialButton(R.id.btnInstagram, "https://www.instagram.com/Sagiv23");
        setupSocialButton(R.id.btnGithub, "https://github.com/sagiv23");
    }

    private void setupSocialButton(int buttonId, String url) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
}
