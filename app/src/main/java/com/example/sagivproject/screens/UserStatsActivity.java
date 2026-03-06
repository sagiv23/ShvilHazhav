package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationUsageAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.MemoryGameDayStats;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.ui.SimpleXYGraphView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserStatsActivity extends BaseActivity {
    private SimpleXYGraphView graphMemoryCW, graphMemoryWins, graphMathCW;
    private RecyclerView recyclerMedicationLogs;
    private MedicationUsageAdapter usageAdapter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userStatsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = sharedPreferencesUtil.getUser();
        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        graphMemoryCW = findViewById(R.id.graph_memory_cw);
        graphMemoryWins = findViewById(R.id.graph_wins);
        graphMathCW = findViewById(R.id.graph_math_stats);
        recyclerMedicationLogs = findViewById(R.id.recycler_medication_logs);

        fetchLatestUserData();
        setupMedicationLogs();
    }

    private void fetchLatestUserData() {
        databaseService.getUserService().getUser(user.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    user = updatedUser;
                    sharedPreferencesUtil.saveUser(user);
                    setupGraphs();
                }
            }

            @Override
            public void onFailed(Exception e) {
                setupGraphs();
            }
        });
    }

    private void setupGraphs() {
        setupMemoryGraphs();
        setupMathGraphs();
    }

    private void setupMemoryGraphs() {
        if (user.getMemoryGameDayStats() == null || user.getMemoryGameDayStats().isEmpty()) return;

        Map<String, MemoryGameDayStats> statsMap = new TreeMap<>(user.getMemoryGameDayStats());
        List<SimpleXYGraphView.Point> ratioPoints = new ArrayList<>();
        List<SimpleXYGraphView.Point> winPoints = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        int index = 0;
        for (String date : statsMap.keySet()) {
            MemoryGameDayStats stats = statsMap.get(date);
            if (stats != null) {
                float total = stats.getCorrectAnswers() + stats.getWrongAnswers();
                float ratio = total > 0 ? (stats.getCorrectAnswers() / total) * 100 : 0;

                ratioPoints.add(new SimpleXYGraphView.Point(index, ratio));
                winPoints.add(new SimpleXYGraphView.Point(index, stats.getWins()));
                dates.add(date);
                index++;
            }
        }

        graphMemoryCW.setData(ratioPoints, dates, "זיכרון: אחוז הצלחה", "תאריך", "% הצלחה");
        graphMemoryWins.setData(winPoints, dates, "זיכרון: ניצחונות", "תאריך", "ניצחונות");
    }

    private void setupMathGraphs() {
        if (user.getMathProblemsDayStats() == null || user.getMathProblemsDayStats().isEmpty())
            return;

        Map<String, MemoryGameDayStats> statsMap = new TreeMap<>(user.getMathProblemsDayStats());
        List<SimpleXYGraphView.Point> ratioPoints = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        int index = 0;
        for (String date : statsMap.keySet()) {
            MemoryGameDayStats stats = statsMap.get(date);
            if (stats != null) {
                float total = stats.getCorrectAnswers() + stats.getWrongAnswers();
                float ratio = total > 0 ? (stats.getCorrectAnswers() / total) * 100 : 0;

                ratioPoints.add(new SimpleXYGraphView.Point(index, ratio));
                dates.add(date);
                index++;
            }
        }

        graphMathCW.setData(ratioPoints, dates, "מתמטיקה: אחוז הצלחה", "תאריך", "% הצלחה");
    }

    private void setupMedicationLogs() {
        recyclerMedicationLogs.setLayoutManager(new LinearLayoutManager(this));
        usageAdapter = adapterService.getMedicationUsageAdapter();
        recyclerMedicationLogs.setAdapter(usageAdapter);

        databaseService.getMedicationService().getMedicationUsageLogs(user.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<MedicationUsage> list) {
                if (list != null) {
                    Collections.reverse(list);
                    usageAdapter.setData(list);
                }
            }

            @Override
            public void onFailed(Exception e) {
            }
        });
    }
}
