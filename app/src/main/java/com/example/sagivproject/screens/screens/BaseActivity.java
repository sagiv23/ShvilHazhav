package com.example.sagivproject.screens.screens;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sagivproject.screens.services.DatabaseService;

public class BaseActivity extends AppCompatActivity {
    protected DatabaseService databaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();
    }
}
