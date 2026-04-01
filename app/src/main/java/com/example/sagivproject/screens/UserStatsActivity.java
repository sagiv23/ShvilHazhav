package com.example.sagivproject.screens;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.GraphAdapter;
import com.example.sagivproject.adapters.MedicationUsageAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.GraphData;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.ui.CustomTypefaceSpan;
import com.example.sagivproject.ui.SimpleXYGraphView;
import com.example.sagivproject.utils.CalendarUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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

/**
 * Activity providing a comprehensive view of user performance statistics and activity logs.
 * <p>
 * This screen features:
 * <ul>
 * <li>Interactive XY graphs for tracking memory game performance, math accuracy, and medication compliance over time.</li>
 * <li>A filterable historical log of medication usage events.</li>
 * <li>Administrative mode: allowing admins to select and view stats for any regular user in the system.</li>
 * <li>Date-based filtering using a calendar picker for usage logs.</li>
 * </ul>
 * It utilizes {@link ViewPager2} with {@link TabLayout} to organize multiple graphs.
 * </p>
 */
@AndroidEntryPoint
public class UserStatsActivity extends BaseActivity {
    /** Internal list of users available for selection in Admin mode. */
    private final List<User> selectableUsers = new ArrayList<>();

    /** Utility for standardized date picking and formatting. */
    @Inject
    protected CalendarUtil calendarUtil;

    private ViewPager2 viewPagerGraphs;
    private GraphAdapter graphAdapter;
    private TabLayout tabLayoutGraphs;

    private RecyclerView recyclerMedicationLogs;
    private MedicationUsageAdapter usageAdapter;

    /** The user whose statistics are currently being displayed. */
    private User currentUser;
    /** The actual authenticated user session. */
    private User loggedInUser;

    private Spinner spinnerUserSelector;
    private TextView txtSelectedDate;
    /** Date string (yyyy-MM-dd) used to filter the usage log. */
    private String filteredDate = null;
    /** The complete list of usage logs for the current user. */
    private List<MedicationUsage> allLogs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.userStatsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        loggedInUser = sharedPreferencesUtil.getUser();
        currentUser = loggedInUser;
        filteredDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        viewPagerGraphs = findViewById(R.id.viewPager_graphs);
        tabLayoutGraphs = findViewById(R.id.tabLayout_graphs);
        recyclerMedicationLogs = findViewById(R.id.recycler_medication_logs);
        spinnerUserSelector = findViewById(R.id.spinner_user_selector);
        txtSelectedDate = findViewById(R.id.txt_user_stats_selected_date);

        findViewById(R.id.btn_user_stats_clear_med_logs).setOnClickListener(v -> clearMedicationLogs());
        findViewById(R.id.btn_user_stats_open_calendar).setOnClickListener(v -> openCalendar());

        setupAdminUI();
        setupGraphsUI();
        refreshData();
        setupMedicationLogs();

        String todayDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        txtSelectedDate.setText(String.format("מציג תוצאות לתאריך: %s (היום)", todayDisplay));
        txtSelectedDate.setVisibility(View.VISIBLE);
    }

    /** Initializes the ViewPager2 and TabLayout used to display the XY graphs. */
    private void setupGraphsUI() {
        graphAdapter = adapterService.getGraphAdapter();
        viewPagerGraphs.setAdapter(graphAdapter);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.text_hebrew);

        new TabLayoutMediator(tabLayoutGraphs, viewPagerGraphs, (tab, position) -> {
            if (position < graphAdapter.getItemList().size()) {
                String title = graphAdapter.getItemList().get(position).getTitle();
                String tabTitle = title.split(":")[0];
                if (typeface != null) {
                    SpannableString s = new SpannableString(tabTitle);
                    s.setSpan(new CustomTypefaceSpan("", typeface), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    s.setSpan(new AbsoluteSizeSpan(18, true), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tab.setText(s);
                } else {
                    tab.setText(tabTitle);
                }
            }
        }).attach();
    }

    /** Opens a date picker to filter the medication usage history. */
    private void openCalendar() {
        calendarUtil.openDatePicker(this, System.currentTimeMillis(), (dateMillis, formattedDate) -> {
            filteredDate = calendarUtil.formatDate(dateMillis, "yyyy-MM-dd");
            txtSelectedDate.setText(String.format("מציג תוצאות לתאריך: %s", formattedDate));
            txtSelectedDate.setVisibility(View.VISIBLE);
            applyFilter();
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT);
    }

    /** Filters the {@link #allLogs} list based on the selected {@link #filteredDate}. */
    private void applyFilter() {
        if (filteredDate == null) {
            usageAdapter.setData(allLogs);
        } else {
            List<MedicationUsage> filtered = allLogs.stream()
                    .filter(log -> filteredDate.equals(log.getDate()))
                    .collect(Collectors.toList());
            usageAdapter.setData(filtered);
            if (filtered.isEmpty() && !allLogs.isEmpty()) {
                txtSelectedDate.setText("אין תיעוד לתאריך זה");
            }
        }
    }

    /** Sets up the administrator-only UI for selecting different users to view their stats. */
    private void setupAdminUI() {
        if (loggedInUser.isAdmin()) {
            findViewById(R.id.card_user_selector).setVisibility(View.VISIBLE);
            databaseService.getUserService().getUserList(new DatabaseCallback<>() {
                @Override
                public void onCompleted(List<User> list) {
                    selectableUsers.clear();
                    List<String> userNames = new ArrayList<>();

                    for (User u : list) {
                        if (!u.isAdmin()) {
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
                            refreshData();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) { Toast.makeText(UserStatsActivity.this, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show(); }
            });
        }
    }

    /** Refreshes the data for the currently selected user from the database. */
    private void refreshData() {
        fetchLatestUserData();
        loadMedicationLogs();
    }

    /** Fetches the latest {@link User} object to ensure graphs display up-to-date information. */
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
            public void onFailed(Exception e) { setupGraphs(); }
        });
    }

    /** Processes historical daily statistics into {@link GraphData} objects for rendering. */
    private void setupGraphs() {
        List<GraphData> graphs = new ArrayList<>();

        if (currentUser.getDailyStats() == null || currentUser.getDailyStats().isEmpty()) {
            graphs.add(new GraphData("memory", "זיכרון: אחוז ניצחונות", new ArrayList<>(), new ArrayList<>(), "תאריך", "% ניצחונות"));
            graphs.add(new GraphData("math", "מתמטיקה: אחוז הצלחה", new ArrayList<>(), new ArrayList<>(), "תאריך", "% הצלחה"));
            graphs.add(new GraphData("meds", "תרופות: עמידה ביעדים", new ArrayList<>(), new ArrayList<>(), "תאריך", "% הצלחה"));
            graphAdapter.setData(graphs);
            return;
        }

        Map<String, DailyStats> statsMap = new TreeMap<>(currentUser.getDailyStats());

        List<SimpleXYGraphView.Point> memoryWinPoints = new ArrayList<>();
        List<String> memoryDates = new ArrayList<>();

        List<SimpleXYGraphView.Point> mathRatioPoints = new ArrayList<>();
        List<String> mathDates = new ArrayList<>();

        List<SimpleXYGraphView.Point> medRatioPoints = new ArrayList<>();
        List<String> medDates = new ArrayList<>();

        int memIdx = 0, mathIdx = 0, medIdx = 0;

        for (Map.Entry<String, DailyStats> entry : statsMap.entrySet()) {
            String date = entry.getKey();
            DailyStats stats = entry.getValue();
            if (stats == null) continue;

            if (stats.getMemoryGamesPlayed() > 0) {
                float winRatio = (stats.getMemoryWins() / (float) stats.getMemoryGamesPlayed()) * 100;
                memoryWinPoints.add(new SimpleXYGraphView.Point(memIdx++, winRatio));
                memoryDates.add(date);
            }

            int totalMath = stats.getMathCorrect() + stats.getMathWrong();
            if (totalMath > 0) {
                float mathRatio = (stats.getMathCorrect() / (float) totalMath) * 100;
                mathRatioPoints.add(new SimpleXYGraphView.Point(mathIdx++, mathRatio));
                mathDates.add(date);
            }

            int totalMeds = stats.getMedicationsTaken() + stats.getMedicationsMissed();
            if (totalMeds > 0) {
                float medRatio = (stats.getMedicationsTaken() / (float) totalMeds) * 100;
                medRatioPoints.add(new SimpleXYGraphView.Point(medIdx++, medRatio));
                medDates.add(date);
            }
        }

        graphs.add(new GraphData("memory", "זיכרון: אחוז ניצחונות", memoryWinPoints, memoryDates, "תאריך", "% ניצחונות"));
        graphs.add(new GraphData("math", "מתמטיקה: אחוז הצלחה", mathRatioPoints, mathDates, "תאריך", "% הצלחה"));
        graphs.add(new GraphData("meds", "תרופות: עמידה ביעדים", medRatioPoints, medDates, "תאריך", "% הצלחה"));

        graphAdapter.setData(graphs);
    }

    /** Configures the RecyclerView for displaying medication logs. */
    private void setupMedicationLogs() {
        recyclerMedicationLogs.setLayoutManager(new LinearLayoutManager(this));
        usageAdapter = adapterService.getMedicationUsageAdapter();
        recyclerMedicationLogs.setAdapter(usageAdapter);
    }

    /** Loads the full medication usage history for the current user. */
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

    /** Clears all historical usage logs for the user after user confirmation. */
    private void clearMedicationLogs() {
        dialogService.showConfirmDialog(getSupportFragmentManager(), "איפוס היסטוריה", "האם לאפס?", "אפס", "בטל", () -> databaseService.getMedicationService().clearMedicationUsageLogs(currentUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                allLogs.clear();
                usageAdapter.setData(new ArrayList<>());
                Toast.makeText(UserStatsActivity.this, "ההיסטוריה אופסה בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(UserStatsActivity.this, "שגיאה באיפוס ההיסטוריה", Toast.LENGTH_SHORT).show(); }
        }));
    }
}