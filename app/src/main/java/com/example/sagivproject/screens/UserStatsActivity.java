package com.example.sagivproject.screens;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import com.example.sagivproject.ui.SimpleXYGraphView;
import com.example.sagivproject.utils.CalendarUtil;
import com.google.android.material.button.MaterialButton;
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
    /**
     * Internal list of users available for selection in Admin mode.
     */
    private final List<User> selectableUsers = new ArrayList<>();

    /**
     * Utility for standardized date picking and formatting.
     */
    @Inject
    protected CalendarUtil calendarUtil;

    private ViewPager2 viewPagerGraphs;
    private GraphAdapter graphAdapter;
    private TabLayout tabLayoutGraphs;

    private RecyclerView recyclerMedicationLogs;
    private MedicationUsageAdapter usageAdapter;

    /**
     * The user whose statistics are currently being displayed.
     */
    private User currentUser;
    /**
     * The actual authenticated user session.
     */
    private User loggedInUser;

    private Spinner spinnerUserSelector;
    private TextView txtSelectedDate;
    private TextView txtNoHistory;
    private MaterialButton btnClearMedLogs;
    /**
     * Date string (yyyy-MM-dd) used to filter the usage log.
     */
    private String filteredDate = null;
    /**
     * The complete list of usage logs for the current user.
     */
    private List<MedicationUsage> allLogs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_user_stats, R.id.userStatsPage);
        setupMenu();

        loggedInUser = sharedPreferencesUtil.getUser();
        currentUser = loggedInUser;
        filteredDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        viewPagerGraphs = findViewById(R.id.viewPager_graphs);
        tabLayoutGraphs = findViewById(R.id.tabLayout_graphs);
        recyclerMedicationLogs = findViewById(R.id.recycler_medication_logs);
        spinnerUserSelector = findViewById(R.id.spinner_user_selector);
        txtSelectedDate = findViewById(R.id.txt_user_stats_selected_date);
        txtNoHistory = findViewById(R.id.txt_user_stats_no_history);

        btnClearMedLogs = findViewById(R.id.btn_user_stats_clear_med_logs);
        btnClearMedLogs.setOnClickListener(v -> clearMedicationLogs());
        btnClearMedLogs.setEnabled(false);

        findViewById(R.id.btn_user_stats_open_calendar).setOnClickListener(v -> openCalendar());

        setupAdminUI();
        setupGraphsUI();
        setupMedicationLogs();

        String todayDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        txtSelectedDate.setText(String.format("מציג תוצאות לתאריך: %s (היום)", todayDisplay));
        txtSelectedDate.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    /**
     * Initializes the ViewPager2 and TabLayout used to display the XY graphs.
     */
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
                    s.setSpan(new TypefaceSpan(typeface), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    s.setSpan(new AbsoluteSizeSpan(18, true), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tab.setText(s);
                } else {
                    tab.setText(tabTitle);
                }
            }
        }).attach();
    }

    /**
     * Opens a date picker to filter the medication usage history.
     */
    private void openCalendar() {
        calendarUtil.openDatePicker(this, filteredDate, (dateMillis, dbDate, formattedDate) -> {
            filteredDate = dbDate;
            applyFilter();
            if (usageAdapter != null && usageAdapter.getItemCount() == 0) {
                dialogService.showConfirmDialog(getSupportFragmentManager(), "שגיאה", "לא נמצא תיעוד לתאריך זה.", "אישור", null, () -> {
                });
            }
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT);
    }

    /**
     * Filters the {@link #allLogs} list based on the selected {@link #filteredDate}.
     */
    private void applyFilter() {
        if (allLogs.isEmpty() && filteredDate == null) {
            txtNoHistory.setVisibility(View.VISIBLE);
            recyclerMedicationLogs.setVisibility(View.GONE);
            txtSelectedDate.setVisibility(View.GONE);
            return;
        }

        txtNoHistory.setVisibility(allLogs.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerMedicationLogs.setVisibility(allLogs.isEmpty() ? View.GONE : View.VISIBLE);
        txtSelectedDate.setVisibility(View.VISIBLE);

        if (filteredDate == null) {
            if (usageAdapter != null) usageAdapter.setData(allLogs);
            txtSelectedDate.setText("מציג את כל ההיסטוריה");
        } else {
            List<MedicationUsage> filtered = allLogs.stream()
                    .filter(log -> filteredDate.equals(log.getDate()))
                    .collect(Collectors.toList());
            if (usageAdapter != null) usageAdapter.setData(filtered);

            String dateDisplay = filteredDate;
            try {
                SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date d = sdfIn.parse(filteredDate);
                if (d != null) {
                    dateDisplay = sdfOut.format(d);
                }
            } catch (Exception ignored) {
            }

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String suffix = filteredDate.equals(today) ? " (היום)" : "";

            if (filtered.isEmpty()) {
                txtSelectedDate.setText(String.format("אין תיעוד לתאריך: %s%s", dateDisplay, suffix));
            } else {
                txtSelectedDate.setText(String.format("מציג תוצאות לתאריך: %s%s", dateDisplay, suffix));
            }
        }
    }

    /**
     * Sets up the administrator-only UI for selecting different users to view their stats.
     */
    private void setupAdminUI() {
        if (loggedInUser.isAdmin()) {
            findViewById(R.id.card_user_selector).setVisibility(View.VISIBLE);
            databaseService.getUserService().getUserList(new DatabaseCallback<>() {
                @Override
                public void onCompleted(List<User> list) {
                    if (list == null || list.isEmpty()) {
                        findViewById(R.id.card_user_selector).setVisibility(View.GONE);
                        return;
                    }

                    selectableUsers.clear();
                    selectableUsers.addAll(list.stream()
                            .filter(u -> !u.isAdmin())
                            .collect(Collectors.toList()));

                    if (selectableUsers.isEmpty()) {
                        findViewById(R.id.card_user_selector).setVisibility(View.GONE);
                        return;
                    }

                    List<String> userNames = selectableUsers.stream()
                            .map(User::getFullName)
                            .collect(Collectors.toList());

                    ArrayAdapter<String> adapter = createStyledSearchAdapter(userNames);

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

                    // Trigger refresh for the first selected user
                    if (!selectableUsers.isEmpty()) {
                        currentUser = selectableUsers.get(0);
                        refreshData();
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(UserStatsActivity.this, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Refreshes the data for the currently selected user from the database.
     */
    private void refreshData() {
        fetchLatestUserData();
        loadMedicationLogs();
    }

    /**
     * Fetches the latest {@link User} object to ensure graphs display up-to-date information.
     */
    private void fetchLatestUserData() {
        final String requestedUserId = currentUser.getId();
        databaseService.getUserService().getUser(requestedUserId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (!requestedUserId.equals(currentUser.getId())) return;
                if (updatedUser != null) {
                    currentUser = updatedUser;
                    if (currentUser.getId().equals(loggedInUser.getId())) {
                        sharedPreferencesUtil.saveUser(currentUser);
                    }
                }
                setupGraphs();
            }

            @Override
            public void onFailed(Exception e) {
                if (!requestedUserId.equals(currentUser.getId())) return;
                setupGraphs();
            }
        });
    }

    /**
     * Processes historical daily statistics into {@link GraphData} objects for rendering.
     */
    private void setupGraphs() {
        if (currentUser.getDailyStats() == null || currentUser.getDailyStats().isEmpty()) {
            graphAdapter.setData(List.of(
                    new GraphData("memory", "זיכרון: אחוז ניצחונות", new ArrayList<>(), new ArrayList<>(), "תאריך", "% ניצחונות"),
                    new GraphData("math", "מתמטיקה: אחוז הצלחה", new ArrayList<>(), new ArrayList<>(), "תאריך", "% הצלחה"),
                    new GraphData("meds", "תרופות: עמידה ביעדים", new ArrayList<>(), new ArrayList<>(), "תאריך", "% הצלחה")
            ));
            return;
        }

        Map<String, DailyStats> statsMap = new TreeMap<>(currentUser.getDailyStats());

        List<SimpleXYGraphView.Point> memoryWinPoints = new ArrayList<>();
        List<String> memoryDates = new ArrayList<>();
        List<SimpleXYGraphView.Point> mathRatioPoints = new ArrayList<>();
        List<String> mathDates = new ArrayList<>();
        List<SimpleXYGraphView.Point> medRatioPoints = new ArrayList<>();
        List<String> medDates = new ArrayList<>();

        final int[] indices = {0, 0, 0}; // 0: memory, 1: math, 2: meds

        statsMap.forEach((date, stats) -> {
            if (stats == null) return;

            if (stats.getMemoryGamesPlayed() > 0) {
                float winRatio = (stats.getMemoryWins() / (float) stats.getMemoryGamesPlayed()) * 100;
                memoryWinPoints.add(new SimpleXYGraphView.Point(indices[0]++, winRatio));
                memoryDates.add(date);
            }

            int totalMath = stats.getMathCorrect() + stats.getMathWrong();
            if (totalMath > 0) {
                float mathRatio = (stats.getMathCorrect() / (float) totalMath) * 100;
                mathRatioPoints.add(new SimpleXYGraphView.Point(indices[1]++, mathRatio));
                mathDates.add(date);
            }

            int totalMeds = stats.getMedicationsTaken() + stats.getMedicationsMissed();
            if (totalMeds > 0) {
                float medRatio = (stats.getMedicationsTaken() / (float) totalMeds) * 100;
                medRatioPoints.add(new SimpleXYGraphView.Point(indices[2]++, medRatio));
                medDates.add(date);
            }
        });

        graphAdapter.setData(List.of(
                new GraphData("memory", "זיכרון: אחוז ניצחונות", memoryWinPoints, memoryDates, "תאריך", "% ניצחונות"),
                new GraphData("math", "מתמטיקה: אחוז הצלחה", mathRatioPoints, mathDates, "תאריך", "% הצלחה"),
                new GraphData("meds", "תרופות: עמידה ביעדים", medRatioPoints, medDates, "תאריך", "% הצלחה")
        ));
    }

    /**
     * Configures the RecyclerView for displaying medication logs.
     */
    private void setupMedicationLogs() {
        recyclerMedicationLogs.setLayoutManager(new LinearLayoutManager(this));
        usageAdapter = adapterService.getMedicationUsageAdapter();
        recyclerMedicationLogs.setAdapter(usageAdapter);
    }

    /**
     * Loads the full medication usage history for the current user.
     */
    private void loadMedicationLogs() {
        final String requestedUserId = currentUser.getId();
        databaseService.getMedicationService().getMedicationUsageLogs(requestedUserId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<MedicationUsage> list) {
                if (!requestedUserId.equals(currentUser.getId())) return;
                if (list != null) {
                    allLogs = new ArrayList<>(list);
                    Collections.reverse(allLogs);
                    applyFilter();
                } else {
                    allLogs.clear();
                    usageAdapter.setData(new ArrayList<>());
                    applyFilter();
                }
                updateResetButtonState();
            }

            @Override
            public void onFailed(Exception e) {
                if (!requestedUserId.equals(currentUser.getId())) return;
                allLogs.clear();
                usageAdapter.setData(new ArrayList<>());
                applyFilter();
                updateResetButtonState();
            }
        });
    }

    /**
     * Clears all historical usage logs for the user after user confirmation.
     */
    private void clearMedicationLogs() {
        dialogService.showConfirmDialog(getSupportFragmentManager(), "איפוס היסטוריה", "האם לאפס?", "אפס", "בטל", () -> databaseService.getMedicationService().clearMedicationUsageLogs(currentUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                allLogs.clear();
                usageAdapter.setData(new ArrayList<>());
                applyFilter();
                updateResetButtonState();
                Toast.makeText(UserStatsActivity.this, "ההיסטוריה אופסה בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserStatsActivity.this, "שגיאה באיפוס ההיסטוריה", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    /**
     * Updates the enabled state of the reset history button based on whether logs exist.
     */
    private void updateResetButtonState() {
        if (btnClearMedLogs != null) {
            btnClearMedLogs.setEnabled(!allLogs.isEmpty());
        }
    }
}