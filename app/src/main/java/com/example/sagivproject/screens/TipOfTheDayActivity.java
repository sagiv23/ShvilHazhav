package com.example.sagivproject.screens;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.TipAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.dialogs.ConfirmDialog;
import com.example.sagivproject.dialogs.TipDialog;
import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseCallback;
import com.example.sagivproject.services.ITTSService;
import com.example.sagivproject.services.ITTSService.TTSListener;
import com.example.sagivproject.services.ITipOfTheDayService;
import com.example.sagivproject.utils.CalendarUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that displays a daily motivational tip and an inspirational quote.
 * <p>
 * This screen performs a daily content check:
 * <ul>
 * <li>It attempts to fetch today's tip from the shared database.</li>
 * <li>If no tip exists for today, it uses the Google Gemini AI model to generate a unique, relevant tip.</li>
 * <li>Generated tips are saved back to the database for all other users to see.</li>
 * <li>It provides Text-to-Speech (TTS) support for both the daily tip and the quote.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public class TipOfTheDayActivity extends BaseActivity {
    /**
     * List of month keys (yyyy-MM) used for grouping and filtering tips.
     */
    private final List<String> currentMonthKeys = new ArrayList<>();

    /**
     * Utility for calendar and date formatting operations.
     */
    @Inject
    protected CalendarUtil calendarUtil;

    /**
     * Singleton service for Text-to-Speech (TTS) functionality.
     */
    @Inject
    protected ITTSService ttsService;
    /**
     * Adapter for managing TipOfTheDay items in the RecyclerView.
     */
    @Inject
    protected TipAdapter tipAdapter;
    @Inject
    protected ITipOfTheDayService tipOfTheDayService;
    @Inject
    protected Provider<TipDialog> tipDialogProvider;
    @Inject
    protected Provider<ConfirmDialog> confirmDialogProvider;
    /**
     * UI components for displaying tip and inspiration content.
     */
    private TextView tipContent, tvInspirationContent, tvSelectedTipDate, tvSelectedTipContent, tvNoTipsError;
    /**
     * Buttons for triggering audio playback of content.
     */
    private Button btnTipSpeak, btnInspirationSpeak, btnSelectedTipSpeak;
    /**
     * RecyclerView for displaying a list of tips (Admin view).
     */
    private RecyclerView rvAllTips;
    /**
     * TabLayout for filtering tips by month.
     */
    private TabLayout tabLayoutMonths;
    /**
     * TabLayout for switching between Tips and Inspirations in Admin view.
     */
    private TabLayout tabLayoutAdmin;
    /**
     * Container view for the details of a selected tip.
     */
    private View cardSelectedTip;

    /**
     * Main scrollable container for the activity's content.
     */
    private NestedScrollView scrollView;

    /**
     * Cached list of all tips fetched from the database.
     */
    private List<TipOfTheDay> allTipsList = new ArrayList<>();

    /**
     * Cached list of all inspirations fetched from the database.
     */
    private List<TipOfTheDay> allInspirationsList = new ArrayList<>();

    /**
     * Google Gemini AI model for generating daily tips when none exist.
     */
    private GenerativeModelFutures model;
    /**
     * Tracks which content ID is currently being played via TTS.
     */
    private String currentlySpeakingId = null;

    /**
     * Initializes the activity, sets up the UI components, and handles admin-specific logic.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_tip_of_the_day, R.id.tipOfTheDayPage);
        setupMenu();

        String currentDate = calendarUtil.formatDate(System.currentTimeMillis());
        ((TextView) findViewById(R.id.tv_tip_of_the_day_date)).setText(currentDate);

        scrollView = findViewById(R.id.nsv_tip_of_the_day);
        tipContent = findViewById(R.id.tv_tipOfTheDay_content);
        tvInspirationContent = findViewById(R.id.tv_tipOfTheDay_inspiration_content);
        btnTipSpeak = findViewById(R.id.btn_tip_speak);
        btnInspirationSpeak = findViewById(R.id.btn_inspiration_speak);
        btnSelectedTipSpeak = findViewById(R.id.btn_selected_tip_speak);
        CalendarView cvTipCalendar = findViewById(R.id.cv_tip_calendar);
        rvAllTips = findViewById(R.id.rv_all_tips);
        View cardInspiration = findViewById(R.id.card_inspiration);
        View cardDailyTip = findViewById(R.id.card_daily_tip);
        cardSelectedTip = findViewById(R.id.card_selected_tip);
        Button btnAddTip = findViewById(R.id.btn_add_tip);
        tvSelectedTipDate = findViewById(R.id.tv_selected_tip_date);
        tvSelectedTipContent = findViewById(R.id.tv_selected_tip_content);
        tvNoTipsError = findViewById(R.id.tv_no_tips_error);
        ImageButton btnCloseSelectedTip = findViewById(R.id.btn_close_selected_tip);
        tabLayoutAdmin = findViewById(R.id.tab_layout_admin);
        tabLayoutMonths = findViewById(R.id.tab_layout_months);

        User currentUser = sharedPreferencesUtil.getUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        if (isAdmin) {
            cardInspiration.setVisibility(View.GONE);
            cardDailyTip.setVisibility(View.GONE);
            tabLayoutAdmin.setVisibility(View.VISIBLE);
            btnAddTip.setVisibility(View.VISIBLE);

            Typeface typeface = ResourcesCompat.getFont(this, R.font.text_hebrew);
            for (int i = 0; i < tabLayoutAdmin.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayoutAdmin.getTabAt(i);
                if (tab != null && typeface != null) {
                    SpannableString s = new SpannableString(tab.getText());
                    s.setSpan(new TypefaceSpan(typeface), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    s.setSpan(new AbsoluteSizeSpan(20, true), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tab.setText(s);
                }
            }

            tipAdapter.setAdmin(true, new TipAdapter.OnTipActionListener() {
                @Override
                public void onEdit(TipOfTheDay tip) {
                    if (tabLayoutAdmin.getSelectedTabPosition() == 0) {
                        showEditTipDialog(tip);
                    } else {
                        showEditInspirationDialog(tip);
                    }
                }

                @Override
                public void onDelete(TipOfTheDay tip) {
                    if (tabLayoutAdmin.getSelectedTabPosition() == 0) {
                        showDeleteTipDialog(tip);
                    } else {
                        showDeleteInspirationDialog(tip);
                    }
                }
            });
            rvAllTips.setLayoutManager(new LinearLayoutManager(this));
            rvAllTips.setAdapter(tipAdapter);

            fetchAllTips();

            tabLayoutAdmin.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        // All Tips
                        tabLayoutMonths.setVisibility(View.VISIBLE);
                        rvAllTips.setVisibility(View.VISIBLE);
                        findViewById(R.id.card_calendar).setVisibility(View.VISIBLE);
                        btnAddTip.setText(R.string.add_new_tip);
                        tvNoTipsError.setText(R.string.no_tips_to_show);
                        if (!currentMonthKeys.isEmpty()) {
                            filterTipsByMonth(currentMonthKeys.get(tabLayoutMonths.getSelectedTabPosition()));
                        }
                    } else {
                        // All Inspirations
                        tabLayoutMonths.setVisibility(View.GONE);
                        rvAllTips.setVisibility(View.VISIBLE);
                        findViewById(R.id.card_calendar).setVisibility(View.GONE);
                        btnAddTip.setText(R.string.add_new_inspiration);
                        tvNoTipsError.setText("אין השראות להצגה");
                        tipAdapter.setData(allInspirationsList);
                        tvNoTipsError.setVisibility(allInspirationsList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    cardSelectedTip.setVisibility(View.GONE);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        } else {
            findViewById(R.id.card_calendar).setVisibility(View.GONE);
        }

        fetchAllInspirations();

        btnTipSpeak.setOnClickListener(v -> toggleSpeech("tip", tipContent.getText().toString()));
        btnInspirationSpeak.setOnClickListener(v -> toggleSpeech("inspiration", tvInspirationContent.getText().toString()));
        btnSelectedTipSpeak.setOnClickListener(v -> toggleSpeech("selected_tip", tvSelectedTipContent.getText().toString()));

        if (!isAdmin) {
            cvTipCalendar.setMaxDate(System.currentTimeMillis());
        }

        cvTipCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            long millis = calendarUtil.getMillisFromDate(year, month, dayOfMonth);
            String dbDate = calendarUtil.formatDate(millis, CalendarUtil.DATABASE_DATE_FORMAT);
            String formattedDate = calendarUtil.formatDate(millis, CalendarUtil.DEFAULT_DATE_FORMAT);

            showTipForDate(dbDate, formattedDate);
        });

        btnAddTip.setOnClickListener(v -> {
            if (tabLayoutAdmin.getSelectedTabPosition() == 0) {
                showEditTipDialog(null);
            } else {
                showEditInspirationDialog(null);
            }
        });

        btnCloseSelectedTip.setOnClickListener(v -> {
            cardSelectedTip.setVisibility(View.GONE);
            if (isAdmin) {
                rvAllTips.setVisibility(View.VISIBLE);
                tvNoTipsError.setVisibility(allTipsList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        model = GenerativeModelFutures.from(generativeModel);
    }

    /**
     * Fetches all tips from the database for admin view.
     */
    private void fetchAllTips() {
        showLoading();
        tipOfTheDayService.getAllTips(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<TipOfTheDay> result) {
                hideLoading();
                if (result != null && !result.isEmpty()) {
                    allTipsList = new ArrayList<>(result);
                    setupMonthTabs(allTipsList);
                    if (tabLayoutAdmin.getSelectedTabPosition() == 0) {
                        tvNoTipsError.setVisibility(View.GONE);
                        tabLayoutMonths.setVisibility(View.VISIBLE);
                        rvAllTips.setVisibility(View.VISIBLE);
                        findViewById(R.id.card_calendar).setVisibility(View.VISIBLE);
                    }
                } else {
                    if (tabLayoutAdmin.getSelectedTabPosition() == 0) {
                        tvNoTipsError.setVisibility(View.VISIBLE);
                        tabLayoutMonths.setVisibility(View.GONE);
                        rvAllTips.setVisibility(View.GONE);
                        findViewById(R.id.card_calendar).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
                tipContent.setText("שגיאה בטעינת כל הטיפים.");
            }
        });
    }

    /**
     * Groups tips by month and sets up the month TabLayout.
     */
    private void setupMonthTabs(List<TipOfTheDay> tips) {
        Map<String, List<TipOfTheDay>> groupedTips = new HashMap<>();
        for (TipOfTheDay tip : tips) {
            if (tip.getId().length() >= 7) {
                String monthKey = tip.getId().substring(0, 7); // yyyy-MM
                groupedTips.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(tip);
            }
        }

        currentMonthKeys.clear();
        currentMonthKeys.addAll(groupedTips.keySet());
        currentMonthKeys.sort(Comparator.reverseOrder());

        tabLayoutMonths.removeAllTabs();
        Typeface typeface = ResourcesCompat.getFont(this, R.font.text_hebrew);

        for (String monthKey : currentMonthKeys) {
            String label = calendarUtil.formatMonthKeyForDisplay(monthKey);

            TabLayout.Tab tab = tabLayoutMonths.newTab();
            if (typeface != null) {
                SpannableString s = new SpannableString(label);
                s.setSpan(new TypefaceSpan(typeface), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                s.setSpan(new AbsoluteSizeSpan(18, true), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tab.setText(s);
            } else {
                tab.setText(label);
            }
            tabLayoutMonths.addTab(tab);
        }

        tabLayoutMonths.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position >= 0 && position < currentMonthKeys.size()) {
                    String monthKey = currentMonthKeys.get(position);
                    filterTipsByMonth(monthKey);
                    updateCalendarToMonth(monthKey);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (!currentMonthKeys.isEmpty()) {
            filterTipsByMonth(currentMonthKeys.get(0));
        }
    }

    /**
     * Filters the list of tips by a specific month key and updates the adapter.
     *
     * @param monthKey The month key in yyyy-MM format.
     */
    private void filterTipsByMonth(String monthKey) {
        List<TipOfTheDay> filteredTips = allTipsList.stream()
                .filter(tip -> tip.getId().startsWith(monthKey))
                .sorted(Comparator.comparing(TipOfTheDay::getId).reversed())
                .collect(Collectors.toList());
        tipAdapter.setData(filteredTips);
        tvNoTipsError.setVisibility(filteredTips.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Updates the CalendarView to show the specified month.
     *
     * @param monthKey The month key in yyyy-MM format.
     */
    private void updateCalendarToMonth(String monthKey) {
        long timeMillis = calendarUtil.parseMonthKey(monthKey);
        if (timeMillis != -1) {
            CalendarView cv = findViewById(R.id.cv_tip_calendar);
            cv.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                cv.setDate(timeMillis, true, true);
                cv.animate().alpha(1f).setDuration(200).start();
            }).start();
        }
    }

    /**
     * Fetches all inspirational quotes from the database.
     * Updates the UI to show a random inspiration and populates the admin list if active.
     */
    private void fetchAllInspirations() {
        tipOfTheDayService.getAllInspirations(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<TipOfTheDay> result) {
                allInspirationsList = result != null ? new ArrayList<>(result) : new ArrayList<>();

                // Display a random inspiration for all users
                if (!allInspirationsList.isEmpty()) {
                    TipOfTheDay randomInspiration = allInspirationsList.get(new Random().nextInt(allInspirationsList.size()));
                    tvInspirationContent.setText(randomInspiration.getTip());
                }

                User currentUser = sharedPreferencesUtil.getUser();
                boolean isAdmin = currentUser != null && currentUser.isAdmin();

                if (isAdmin && tabLayoutAdmin.getSelectedTabPosition() == 1) {
                    tipAdapter.setData(allInspirationsList);
                    tvNoTipsError.setVisibility(allInspirationsList.isEmpty() ? View.VISIBLE : View.GONE);
                    rvAllTips.setVisibility(allInspirationsList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailed(Exception e) {
                // Fail silently or show error
            }
        });
    }

    /**
     * Opens a dialog to create or edit an inspiration.
     *
     * @param inspiration The inspiration to edit, or null for a new one.
     */
    private void showEditInspirationDialog(@Nullable TipOfTheDay inspiration) {
        TipDialog dialog = tipDialogProvider.get();
        dialog.setInspirationMode(true);
        dialog.setData(inspiration, updatedInspiration -> {
            showLoading();
            tipOfTheDayService.saveInspiration(updatedInspiration, new DatabaseCallback<>() {
                @Override
                public void onCompleted(Void result) {
                    hideLoading();
                    android.widget.Toast.makeText(TipOfTheDayActivity.this, "ההשראה נשמרה", android.widget.Toast.LENGTH_SHORT).show();
                    fetchAllInspirations();
                }

                @Override
                public void onFailed(Exception e) {
                    hideLoading();
                    android.widget.Toast.makeText(TipOfTheDayActivity.this, "שגיאה בשמירת ההשראה", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "InspirationDialog");
    }

    /**
     * Opens a confirmation dialog before deleting an inspiration.
     *
     * @param inspiration The inspiration to delete.
     */
    private void showDeleteInspirationDialog(TipOfTheDay inspiration) {
        ConfirmDialog dialog = confirmDialogProvider.get();
        dialog.setData("מחיקת השראה", "האם למחוק השראה זו?", "מחק", "ביטול", () -> {
            showLoading();
            tipOfTheDayService.deleteInspiration(inspiration.getId(), new DatabaseCallback<>() {
                @Override
                public void onCompleted(Void result) {
                    hideLoading();
                    fetchAllInspirations();
                }

                @Override
                public void onFailed(Exception e) {
                    hideLoading();
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "DeleteInspirationDialog");
    }

    /**
     * Displays the tip for a specific date at the bottom of the screen.
     *
     * @param dateId        The date ID in yyyymmdd format.
     * @param formattedDate The formatted date for display.
     */
    private void showTipForDate(String dateId, String formattedDate) {
        User currentUser = sharedPreferencesUtil.getUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        TipOfTheDay foundTip = allTipsList.stream()
                .filter(tip -> tip.getId().equals(dateId))
                .findFirst()
                .orElse(null);

        if (foundTip != null) {
            if (isAdmin) {
                rvAllTips.setVisibility(View.GONE);
                tvNoTipsError.setVisibility(View.GONE);
            }
            displaySelectedTip(foundTip, formattedDate);
        } else {
            showLoading();
            tipOfTheDayService.getTipByDate(dateId, new DatabaseCallback<>() {
                @Override
                public void onCompleted(TipOfTheDay result) {
                    hideLoading();
                    if (result != null) {
                        if (isAdmin) {
                            rvAllTips.setVisibility(View.GONE);
                            tvNoTipsError.setVisibility(View.GONE);
                        }
                        displaySelectedTip(result, formattedDate);
                    } else {
                        String todayId = calendarUtil.getCurrentDate();
                        if (isAdmin && dateId.equals(todayId)) {
                            android.widget.Toast.makeText(TipOfTheDayActivity.this, "יוצר טיפ יומי באמצעות AI...", android.widget.Toast.LENGTH_SHORT).show();
                            fetchDailyTipFromAI();
                        } else {
                            ConfirmDialog dialog = confirmDialogProvider.get();
                            dialog.setData("שגיאה", "לא נמצא טיפ לתאריך זה.", "אישור", null, () -> {
                            });
                            dialog.show(getSupportFragmentManager(), "ErrorDialog");
                        }
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    hideLoading();
                    ConfirmDialog dialog = confirmDialogProvider.get();
                    dialog.setData("שגיאה", "שגיאה בחיפוש הטיפ.", "אישור", null, () -> {
                    });
                    dialog.show(getSupportFragmentManager(), "SearchErrorDialog");
                }
            });
        }
    }

    /**
     * Displays a dialog for adding a new tip or editing an existing one.
     *
     * @param tip The tip to edit, or null to create a new tip.
     */
    private void showEditTipDialog(@Nullable TipOfTheDay tip) {
        List<String> existingDates = allTipsList.stream()
                .map(TipOfTheDay::getId)
                .collect(Collectors.toList());

        TipDialog dialog = tipDialogProvider.get();
        dialog.setInspirationMode(false);
        dialog.setData(tip, existingDates, updatedTip -> {
            showLoading();
            tipOfTheDayService.saveTip(updatedTip, new DatabaseCallback<>() {
                @Override
                public void onCompleted(Void result) {
                    hideLoading();
                    android.widget.Toast.makeText(TipOfTheDayActivity.this, "הטיפ נשמר בהצלחה", android.widget.Toast.LENGTH_SHORT).show();
                    fetchAllTips();
                    checkDailyTip();
                }

                @Override
                public void onFailed(Exception e) {
                    hideLoading();
                    android.widget.Toast.makeText(TipOfTheDayActivity.this, "שגיאה בשמירת הטיפ", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "EditTipDialog");
    }

    /**
     * Displays a confirmation dialog for deleting a specific tip.
     *
     * @param tip The tip to be deleted.
     */
    private void showDeleteTipDialog(TipOfTheDay tip) {
        ConfirmDialog dialog = confirmDialogProvider.get();
        dialog.setData("מחיקת טיפ", "האם אתה בטוח שברצונך למחוק את הטיפ הזה?", "מחק", "ביטול", () -> {
            showLoading();
            tipOfTheDayService.deleteTip(tip.getId(), new DatabaseCallback<>() {
                @Override
                public void onCompleted(Void result) {
                    hideLoading();
                    android.widget.Toast.makeText(TipOfTheDayActivity.this, "הטיפ נמחק", android.widget.Toast.LENGTH_SHORT).show();
                    fetchAllTips();
                    checkDailyTip();
                }

                @Override
                public void onFailed(Exception e) {
                    hideLoading();
                    android.widget.Toast.makeText(TipOfTheDayActivity.this, "שגיאה במחיקת הטיפ", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "DeleteTipDialog");
    }

    /**
     * Updates the UI with the selected tip and scrolls the view to the bottom.
     *
     * @param tip           The tip to display.
     * @param formattedDate The date to show alongside the tip.
     */
    private void displaySelectedTip(TipOfTheDay tip, String formattedDate) {
        cardSelectedTip.setVisibility(View.VISIBLE);
        tvSelectedTipDate.setText(formattedDate);
        tvSelectedTipContent.setText(tip.getTip());

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * Toggles the audio playback for a specific text block.
     * If the same content is already playing, it stops. Otherwise, it starts playing the new content.
     *
     * @param id   Unique ID for the content piece (e.g., "tip" or "inspiration").
     * @param text The actual text to speak.
     */
    private void toggleSpeech(String id, String text) {
        if (id.equals(currentlySpeakingId)) {
            ttsService.stop();
            updateSpeakButton(id, false);
        } else {
            if (currentlySpeakingId != null) ttsService.stop();
            ttsService.speak(text, id, new TTSListener() {
                @Override
                public void onStart(String id) {
                    runOnUiThread(() -> updateSpeakButton(id, true));
                }

                @Override
                public void onDone(String id) {
                    runOnUiThread(() -> updateSpeakButton(id, false));
                }

                @Override
                public void onError(String id) {
                    runOnUiThread(() -> updateSpeakButton(id, false));
                }
            });
        }
    }

    /**
     * Updates the UI state of the relevant playback button.
     *
     * @param id       The content ID.
     * @param speaking true if currently playing.
     */
    private void updateSpeakButton(String id, boolean speaking) {
        Button btn;
        switch (id) {
            case "tip":
                btn = btnTipSpeak;
                break;
            case "inspiration":
                btn = btnInspirationSpeak;
                break;
            case "selected_tip":
                btn = btnSelectedTipSpeak;
                break;
            default:
                return;
        }
        currentlySpeakingId = speaking ? id : null;
        btn.setText(speaking ? R.string.cancel_playback : R.string.playback);
    }

    /**
     * Checks if today's tip is already in the database and displays it.
     * If not found, it triggers the AI generation process.
     */
    private void checkDailyTip() {
        showLoading();
        tipOfTheDayService.getTipForToday(new DatabaseCallback<>() {
            @Override
            public void onCompleted(TipOfTheDay result) {
                hideLoading();
                if (result != null) {
                    tipContent.setText(result.getTip());
                    btnTipSpeak.setVisibility(View.VISIBLE);
                    if (allTipsList.isEmpty()) allTipsList.add(result);
                } else fetchDailyTipFromAI();
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
                tipContent.setText("שגיאה בקבלת הטיפ היומי.");
            }
        });
    }

    /**
     * Uses Vertex AI (Gemini) to generate a localized daily tip in Hebrew.
     * Once generated, the tip is saved to the database to ensure all users see the same tip.
     */
    private void fetchDailyTipFromAI() {
        showLoading();
        tipContent.setText("טוען טיפ יומי...");
        btnTipSpeak.setVisibility(View.GONE);

        String prompt = "תן טיפ יומי קצר לחיים בעברית. עד 6 משפטים. בלי אימוג'ים. בבקשה תשלח רק את התשובה בלי הקדמה מיותרת";
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);
        Executor mainExecutor = ContextCompat.getMainExecutor(this);

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text == null) text = "לא התקבלה תשובה.";

                String today = calendarUtil.getCurrentDate();
                TipOfTheDay newTip = new TipOfTheDay(text, today);

                String finalText = text;
                tipOfTheDayService.saveTipIfNotExists(newTip, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(TipOfTheDay finalResult) {
                        hideLoading();
                        String tipToDisplay = (finalResult != null) ? finalResult.getTip() : finalText;
                        tipContent.setAlpha(0f);
                        tipContent.setText(tipToDisplay);
                        tipContent.animate().alpha(1f).setDuration(800).withEndAction(() -> btnTipSpeak.setVisibility(View.VISIBLE));
                        if (allTipsList.isEmpty() && finalResult != null)
                            allTipsList.add(finalResult);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        hideLoading();
                        tipContent.setText("שגיאה בשמירת הטיפ היומי.");
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                hideLoading();
                tipContent.setText(String.format("שגיאה: %s", t.getMessage()));
            }
        }, mainExecutor);
    }

    /**
     * Called when the activity is becoming visible to the user.
     * Refreshes the daily tip display.
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkDailyTip();
    }

    /**
     * Called when the activity is no longer in the foreground.
     * Stops any ongoing TTS playback.
     */
    @Override
    protected void onPause() {
        super.onPause();

        ttsService.stop();
        if (currentlySpeakingId != null) {
            updateSpeakButton(currentlySpeakingId, false);
        }
    }

    /**
     * Releases Text-to-Speech resources and performs final cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        if (ttsService != null) {
            ttsService.stop();
        }
        super.onDestroy();
    }
}