package com.example.sagivproject.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.sagivproject.services.DatabaseService;

public abstract class BaseWorkerActivity extends Worker {
    protected DatabaseService databaseService;

    public BaseWorkerActivity(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        databaseService = DatabaseService.getInstance();
    }
}