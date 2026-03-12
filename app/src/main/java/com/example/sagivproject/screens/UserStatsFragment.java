package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationUsageAdapter;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.User;
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

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment to display user statistics and graphs.
 */
@AndroidEntryPoint
public class UserStatsFragment extends BaseFragment {
    private final List<User> selectableUsers = new ArrayList<>();
    private SimpleXYGraphView graphMemoryWins, graphMathStats, graphMedicationStats;
    private RecyclerView recyclerMedicationLogs;
    private MedicationUsageAdapter usageAdapter;
    private User currentUser, loggedInUser;
    private Spinner spinnerUserSelector;
    private TextView txtSelectedDate;
    private String filteredDate = null;
    private List<MedicationUsage> allLogs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loggedInUser = sharedPreferencesUtil.getUser();
        currentUser = loggedInUser;
        filteredDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        graphMemoryWins = view.findViewById(R.id.graph_wins);
        graphMathStats = view.findViewById(R.id.graph_math_stats);
        graphMedicationStats = view.findViewById(R.id.graph_memory_cw);
        recyclerMedicationLogs = view.findViewById(R.id.recycler_medication_logs);
        spinnerUserSelector = view.findViewById(R.id.spinner_user_selector);
        txtSelectedDate = view.findViewById(R.id.txt_user_stats_selected_date);

        view.findViewById(R.id.btn_user_stats_clear_med_logs).setOnClickListener(v -> clearMedicationLogs());
        view.findViewById(R.id.btn_user_stats_open_calendar).setOnClickListener(v -> openCalendar());

        setupAdminUI();
        refreshData();
        setupMedicationLogs();

        String todayDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        txtSelectedDate.setText(String.format("מציג תוצאות לתאריך: %s (היום)", todayDisplay));
        txtSelectedDate.setVisibility(View.VISIBLE);
    }

    private void openCalendar() {
        if (getActivity() == null) return;
        calendarUtil.openDatePicker(getActivity(), System.currentTimeMillis(), (dateMillis, formattedDate) -> {
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
                txtSelectedDate.setText("אין תיעוד לתאריך זה");
            }
        }
    }

    private void setupAdminUI() {
        if (loggedInUser.isAdmin()) {
            if (getView() != null) {
                getView().findViewById(R.id.card_user_selector).setVisibility(View.VISIBLE);
            }
            databaseService.getUserService().getUserList(new DatabaseCallback<>() {
                @Override
                public void onCompleted(List<User> list) {
                    selectableUsers.clear();
                    List<String> userNames = new ArrayList<>();

                    // Filter out the logged-in admin from the list
                    for (User u : list) {
                        if (!u.isAdmin()) {
                            selectableUsers.add(u);
                            userNames.add(u.getFullName());
                        }
                    }

                    if (selectableUsers.isEmpty()) {
                        if (getView() != null) {
                            getView().findViewById(R.id.card_user_selector).setVisibility(View.GONE);
                        }
                        return;
                    }

                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, userNames) {
                            @NonNull
                            @Override
                            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                                TextView tv = (TextView) super.getView(position, convertView, parent);
                                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.text_hebrew));
                                tv.setTextSize(22);
                                tv.setTextColor(getColor(R.color.text_color));
                                tv.setPadding(24, 24, 24, 24);
                                return tv;
                            }

                            @Override
                            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.text_hebrew));
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
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(requireContext(), "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void refreshData() {
        fetchLatestUserData();
        loadMedicationLogs();
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
            graphMemoryWins.setData(new ArrayList<>(), new ArrayList<>(), "זיכרון: אחוז ניצחונות", "תאריך", "% ניצחונות");
            graphMathStats.setData(new ArrayList<>(), new ArrayList<>(), "מתמטיקה: אחוז הצלחה", "תאריך", "% הצלחה");
            graphMedicationStats.setData(new ArrayList<>(), new ArrayList<>(), "תרופות: עמידה ביעדים", "תאריך", "% הצלחה");
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

            // Filter Memory: Only if games were actually played on that day
            if (stats.getMemoryGamesPlayed() > 0) {
                float winRatio = (stats.getMemoryWins() / (float) stats.getMemoryGamesPlayed()) * 100;
                memoryWinPoints.add(new SimpleXYGraphView.Point(memIdx++, winRatio));
                memoryDates.add(date);
            }

            // Filter Math: Only if problems were attempted
            int totalMath = stats.getMathCorrect() + stats.getMathWrong();
            if (totalMath > 0) {
                float mathRatio = (stats.getMathCorrect() / (float) totalMath) * 100;
                mathRatioPoints.add(new SimpleXYGraphView.Point(mathIdx++, mathRatio));
                mathDates.add(date);
            }

            // Filter Medications: Only if taken/missed was recorded
            int totalMeds = stats.getMedicationsTaken() + stats.getMedicationsMissed();
            if (totalMeds > 0) {
                float medRatio = (stats.getMedicationsTaken() / (float) totalMeds) * 100;
                medRatioPoints.add(new SimpleXYGraphView.Point(medIdx++, medRatio));
                medDates.add(date);
            }
        }

        graphMemoryWins.setData(memoryWinPoints, memoryDates, "זיכרון: אחוז ניצחונות", "תאריך", "% ניצחונות");
        graphMathStats.setData(mathRatioPoints, mathDates, "מתמטיקה: אחוז הצלחה", "תאריך", "% הצלחה");
        graphMedicationStats.setData(medRatioPoints, medDates, "תרופות: עמידה ביעדים", "תאריך", "% הצלחה");
    }

    private void setupMedicationLogs() {
        recyclerMedicationLogs.setLayoutManager(new LinearLayoutManager(getContext()));
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

    private void clearMedicationLogs() {
        dialogService.showConfirmDialog(getParentFragmentManager(), "איפוס היסטוריה", "האם לאפס?", "אפס", "בטל", () -> databaseService.getMedicationService().clearMedicationUsageLogs(currentUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                allLogs.clear();
                usageAdapter.setData(new ArrayList<>());
                Toast.makeText(requireContext(), "ההיסטוריה אופסה בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה באיפוס ההיסטוריה", Toast.LENGTH_SHORT).show();
            }
        }));
    }
}
