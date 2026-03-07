package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationUsageAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.UserRole;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.ui.SimpleXYGraphView;
import com.example.sagivproject.utils.CalendarUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserStatsActivity extends BaseActivity {
    private final List<User> selectableUsers = new ArrayList<>();
    @Inject
    CalendarUtil calendarUtil;
    private SimpleXYGraphView graphMemoryWins, graphMathStats, graphMedicationStats;
    private RecyclerView recyclerMedicationLogs;
    private MedicationUsageAdapter usageAdapter;
    private User currentUser;
    private User loggedInUser;
    private Spinner spinnerUserSelector;
    private TextView txtSelectedDate;
    private String filteredDate = null;
    private List<MedicationUsage> allLogs = new ArrayList<>();

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

        loggedInUser = sharedPreferencesUtil.getUser();
        currentUser = loggedInUser;

        // Set default filter to today
        filteredDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        graphMemoryWins = findViewById(R.id.graph_wins);
        graphMathStats = findViewById(R.id.graph_math_stats);
        graphMedicationStats = findViewById(R.id.graph_memory_cw);
        recyclerMedicationLogs = findViewById(R.id.recycler_medication_logs);
        spinnerUserSelector = findViewById(R.id.spinner_user_selector);
        txtSelectedDate = findViewById(R.id.txt_user_stats_selected_date);

        Button btnClearLogs = findViewById(R.id.btn_user_stats_clear_med_logs);
        if (btnClearLogs != null) {
            btnClearLogs.setOnClickListener(v -> clearMedicationLogs());
        }

        findViewById(R.id.btn_user_stats_open_calendar).setOnClickListener(v -> openCalendar());

        setupAdminUI();
        refreshData();
        setupMedicationLogs();

        // Show today's date in the label
        String todayDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        txtSelectedDate.setText(String.format("מציג תוצאות לתאריך: %s (היום)", todayDisplay));
        txtSelectedDate.setVisibility(View.VISIBLE);
    }

    private void openCalendar() {
        calendarUtil.openDatePicker(this, System.currentTimeMillis(), (dateMillis, formattedDate) -> {
            filteredDate = calendarUtil.formatDate(dateMillis, "yyyy-MM-dd");
            txtSelectedDate.setText(String.format("מציג תוצאות לתאריך: %s", formattedDate));
            txtSelectedDate.setVisibility(View.VISIBLE);
            applyFilter();
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT);
    }

    private void applyFilter() {
        if (filteredDate == null) {
            usageAdapter.setData(allLogs);
        } else {
            List<MedicationUsage> filtered = allLogs.stream()
                    .filter(log -> filteredDate.equals(log.getDate()))
                    .collect(Collectors.toList());
            usageAdapter.setData(filtered);
            if (filtered.isEmpty() && !allLogs.isEmpty()) {
                Toast.makeText(this, "אין תיעוד לתאריך זה", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupAdminUI() {
        if (loggedInUser.getRole() == UserRole.ADMIN) {
            findViewById(R.id.card_user_selector).setVisibility(View.VISIBLE);
            databaseService.getUserService().getUserList(new DatabaseCallback<>() {
                @Override
                public void onCompleted(List<User> list) {
                    selectableUsers.clear();
                    List<String> userNames = new ArrayList<>();

                    // Filter out the logged-in admin from the list
                    for (User u : list) {
                        if (!u.getId().equals(loggedInUser.getId())) {
                            selectableUsers.add(u);
                            userNames.add(u.getFullName());
                        }
                    }

                    if (selectableUsers.isEmpty()) {
                        findViewById(R.id.card_user_selector).setVisibility(View.GONE);
                        return;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(UserStatsActivity.this, android.R.layout.simple_spinner_item, userNames) {
                        @NonNull
                        @Override
                        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                            TextView tv = (TextView) super.getView(position, convertView, parent);
                            tv.setTypeface(ResourcesCompat.getFont(UserStatsActivity.this, R.font.text_hebrew));
                            tv.setTextSize(22);
                            tv.setTextColor(getColor(R.color.text_color));
                            tv.setPadding(24, 24, 24, 24);
                            return tv;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                            TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                            tv.setTypeface(ResourcesCompat.getFont(UserStatsActivity.this, R.font.text_hebrew));
                            tv.setTextSize(22);
                            tv.setTextColor(getColor(R.color.text_color));
                            tv.setBackgroundColor(getColor(R.color.background_color_buttons));
                            tv.setPadding(24, 24, 24, 24);
                            return tv;
                        }
                    };

                    spinnerUserSelector.setAdapter(adapter);
                    spinnerUserSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            currentUser = selectableUsers.get(position);
                            // Keep the date filter when switching users
                            refreshData();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(UserStatsActivity.this, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void refreshData() {
        fetchLatestUserData();
        loadMedicationLogs();
    }

    private void clearMedicationLogs() {
        dialogService.showConfirmDialog("איפוס היסטוריה", "האם אתה בטוח שברצונך למחוק את כל יומן נטילת התרופות?", "אפס", "בטל", () -> databaseService.getMedicationService().clearMedicationUsageLogs(currentUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                allLogs.clear();
                usageAdapter.setData(new ArrayList<>());
                Toast.makeText(UserStatsActivity.this, "ההיסטוריה אופסה בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserStatsActivity.this, "שגיאה באיפוס ההיסטוריה", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void fetchLatestUserData() {
        databaseService.getUserService().getUser(currentUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    currentUser = updatedUser;
                    if (currentUser.getId().equals(loggedInUser.getId())) {
                        sharedPreferencesUtil.saveUser(currentUser);
                    }
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
        if (currentUser.getDailyStats() == null || currentUser.getDailyStats().isEmpty()) {
            graphMemoryWins.setData(new ArrayList<>(), new ArrayList<>(), "זיכרון: ניצחונות", "תאריך", "ניצחונות");
            graphMathStats.setData(new ArrayList<>(), new ArrayList<>(), "מתמטיקה: אחוז הצלחה", "תאריך", "% הצלחה");
            graphMedicationStats.setData(new ArrayList<>(), new ArrayList<>(), "תרופות: עמידה ביעדים", "תאריך", "% הצלחה");
            return;
        }

        Map<String, DailyStats> statsMap = new TreeMap<>(currentUser.getDailyStats());
        List<SimpleXYGraphView.Point> memoryWinPoints = new ArrayList<>();
        List<SimpleXYGraphView.Point> mathRatioPoints = new ArrayList<>();
        List<SimpleXYGraphView.Point> medRatioPoints = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        int index = 0;
        for (String date : statsMap.keySet()) {
            DailyStats stats = statsMap.get(date);
            if (stats != null) {
                memoryWinPoints.add(new SimpleXYGraphView.Point(index, stats.getMemoryWins()));

                float totalMath = stats.getMathCorrect() + stats.getMathWrong();
                float mathRatio = totalMath > 0 ? (stats.getMathCorrect() / totalMath) * 100 : 0;
                mathRatioPoints.add(new SimpleXYGraphView.Point(index, mathRatio));

                float totalMeds = stats.getMedicationsTaken() + stats.getMedicationsMissed();
                float medRatio = totalMeds > 0 ? (stats.getMedicationsTaken() / totalMeds) * 100 : 0;
                medRatioPoints.add(new SimpleXYGraphView.Point(index, medRatio));

                dates.add(date);
                index++;
            }
        }

        graphMemoryWins.setData(memoryWinPoints, dates, "זיכרון: ניצחונות", "תאריך", "ניצחונות");
        graphMathStats.setData(mathRatioPoints, dates, "מתמטיקה: אחוז הצלחה", "תאריך", "% הצלחה");
        graphMedicationStats.setData(medRatioPoints, dates, "תרופות: עמידה ביעדים", "תאריך", "% הצלחה");
    }

    private void setupMedicationLogs() {
        recyclerMedicationLogs.setLayoutManager(new LinearLayoutManager(this));
        usageAdapter = adapterService.getMedicationUsageAdapter();
        recyclerMedicationLogs.setAdapter(usageAdapter);
    }

    private void loadMedicationLogs() {
        databaseService.getMedicationService().getMedicationUsageLogs(currentUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<MedicationUsage> list) {
                if (list != null) {
                    allLogs = new ArrayList<>(list);
                    Collections.reverse(allLogs);
                    applyFilter();
                } else {
                    allLogs.clear();
                    usageAdapter.setData(new ArrayList<>());
                }
            }

            @Override
            public void onFailed(Exception e) {
                allLogs.clear();
                usageAdapter.setData(new ArrayList<>());
            }
        });
    }
}
