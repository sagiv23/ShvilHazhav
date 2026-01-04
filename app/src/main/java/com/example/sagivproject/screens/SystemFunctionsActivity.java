package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;

public class SystemFunctionsActivity extends AppCompatActivity {
    private Button btnToAdminPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_system_functions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.systemFunctionsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToAdminPage = findViewById(R.id.btn_SystemFunctionsPage_to_admin);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(SystemFunctionsActivity.this, AdminPageActivity.class)));
    }
}