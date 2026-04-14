package com.example.sagivproject.screens;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.Operation;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.google.android.material.card.MaterialCardView;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for practicing various math problems to maintain cognitive health.
 * <p>
 * This screen generates random arithmetic challenges (addition, subtraction, multiplication,
 * division, powers, and square roots). It features:
 * <ul>
 * <li>Dynamic problem generation with randomized operations and numbers.</li>
 * <li>A custom on-screen numeric keypad for input.</li>
 * <li>Immediate visual feedback for correct or incorrect answers.</li>
 * <li>Daily performance tracking (correct/wrong counts) synchronized with Firebase.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public class MathProblemsActivity extends BaseActivity {
    /**
     * Buffer for current user-entered answer string.
     */
    private final StringBuilder userInput = new StringBuilder();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private TextView tvCorrect, tvWrong;
    private User user;
    private TextView tvQuestion, tvAnswer;
    private MaterialCardView cvAnswerContainer;

    /**
     * The correct answer for the current active problem.
     */
    private int correctAnswer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_math_problems, R.id.mathProblemsPage);
        setupMenu();

        user = sharedPreferencesUtil.getUser();

        tvCorrect = findViewById(R.id.tv_MathProblemsPage_correct);
        tvWrong = findViewById(R.id.tv_MathProblemsPage_wrong);
        tvQuestion = findViewById(R.id.tv_MathProblemsPage_question);
        tvAnswer = findViewById(R.id.tv_MathProblemsPage_user_answer);
        cvAnswerContainer = findViewById(R.id.cv_MathProblemsPage_answer_container);

        fetchLatestStats();
        generateProblem();
        setupKeypad();
    }

    /**
     * Refreshes the user's daily statistics from the database to ensure the UI is current.
     */
    private void fetchLatestStats() {
        databaseService.getUserService().getUser(user.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    user = updatedUser;
                    sharedPreferencesUtil.saveUser(user);
                    updateStatsUI();
                }
            }

            @Override
            public void onFailed(Exception e) {
                updateStatsUI();
            }
        });
    }

    /**
     * Updates the text views displaying the count of correct and incorrect answers for today.
     */
    private void updateStatsUI() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyStats stats = user.getDailyStats().get(today);
        int correct = (stats != null) ? stats.getMathCorrect() : 0;
        int wrong = (stats != null) ? stats.getMathWrong() : 0;

        tvCorrect.setText(MessageFormat.format("נכונות: {0}", correct));
        tvWrong.setText(MessageFormat.format("טעויות: {0}", wrong));
    }

    /**
     * Randomly selects a mathematical operation and generates a new problem.
     * Updates the question display and clears the user input buffer.
     */
    private void generateProblem() {
        Operation operation = Operation.values()[(int) (Math.random() * Operation.values().length)];
        int a, b;

        switch (operation) {
            case ADD:
                a = rand(10, 99);
                b = rand(10, 99);
                correctAnswer = a + b;
                tvQuestion.setText(MessageFormat.format("{0} + {1} =", a, b));
                break;
            case SUBTRACT:
                a = rand(10, 99);
                b = rand(10, a);
                correctAnswer = a - b;
                tvQuestion.setText(MessageFormat.format("{0} - {1} =", a, b));
                break;
            case MULTIPLY:
                a = rand(2, 12);
                b = rand(2, 12);
                correctAnswer = a * b;
                tvQuestion.setText(MessageFormat.format("{0} × {1} =", a, b));
                break;
            case DIVIDE:
                b = rand(2, 12);
                correctAnswer = rand(2, 12);
                a = b * correctAnswer;
                tvQuestion.setText(MessageFormat.format("{0} ÷ {1} =", a, b));
                break;
            case POWER:
                a = rand(2, 5);
                b = rand(2, 3);
                correctAnswer = (int) Math.pow(a, b);
                tvQuestion.setText(MessageFormat.format("{0}^{1} =", a, b));
                break;
            case SQRT:
                correctAnswer = rand(2, 12);
                a = correctAnswer * correctAnswer;
                tvQuestion.setText(MessageFormat.format("√{0} =", a));
                break;
        }
        userInput.setLength(0);
        tvAnswer.setText("");
    }

    /**
     * Generates a random integer within a specified range.
     *
     * @param min Minimum value (inclusive).
     * @param max Maximum value (inclusive).
     * @return A random integer.
     */
    private int rand(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * Initializes the numeric keypad by attaching click listeners to all digit buttons.
     */
    private void setupKeypad() {
        GridLayout keypad = findViewById(R.id.keypad_MathProblemsPage);
        for (int i = 0; i < keypad.getChildCount(); i++) {
            View v = keypad.getChildAt(i);
            if (v instanceof Button) {
                Button btn = (Button) v;
                String text = btn.getText().toString();
                if (text.matches("\\d+")) {
                    btn.setOnClickListener(v1 -> {
                        userInput.append(text);
                        tvAnswer.setText(userInput.toString());
                    });
                }
            }
        }
        findViewById(R.id.btn_MathProblemsPage_delete).setOnClickListener(v -> deleteLast());
        findViewById(R.id.btn_MathProblemsPage_clear).setOnClickListener(v -> clearInput());
        findViewById(R.id.btn_MathProblemsPage_submit).setOnClickListener(v -> checkAnswer());
    }

    /**
     * Removes the last digit from the user's current input.
     */
    private void deleteLast() {
        if (userInput.length() > 0) {
            userInput.deleteCharAt(userInput.length() - 1);
            tvAnswer.setText(userInput.toString());
        }
    }

    /**
     * Resets the user's current input buffer.
     */
    private void clearInput() {
        userInput.setLength(0);
        tvAnswer.setText("");
    }

    /**
     * Validates the user's answer against the correct result.
     * Updates the database, refreshes local statistics, and triggers visual feedback.
     */
    private void checkAnswer() {
        if (userInput.length() == 0) return;
        int userAnswer = Integer.parseInt(userInput.toString());
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (userAnswer == correctAnswer) {
            getTodayStats(today).addMathCorrect();
            showFeedback(true);
            generateProblem();
            databaseService.getStatsService().updateDailyMathStats(user.getId(), true);
        } else {
            getTodayStats(today).addMathWrong();
            showFeedback(false);
            databaseService.getStatsService().updateDailyMathStats(user.getId(), false);
        }

        sharedPreferencesUtil.saveUser(user);
        updateStatsUI();
    }

    /**
     * Retrieves the daily statistics for today, creating a new entry if necessary.
     *
     * @param date Today's date string.
     * @return The {@link DailyStats} object for the current date.
     */
    private DailyStats getTodayStats(String date) {
        if (user.getDailyStats() == null) user.setDailyStats(new HashMap<>());
        if (!user.getDailyStats().containsKey(date)) {
            user.getDailyStats().put(date, new DailyStats());
        }
        return user.getDailyStats().get(date);
    }

    /**
     * Animates the answer container to provide feedback on the answer correctness.
     *
     * @param isCorrect true if the user's answer was right.
     */
    private void showFeedback(boolean isCorrect) {
        int colorRes = isCorrect ? R.color.headline : R.color.error;
        int color = ContextCompat.getColor(this, colorRes);
        cvAnswerContainer.setStrokeColor(ColorStateList.valueOf(color));
        tvAnswer.setTextColor(color);
        handler.postDelayed(() -> {
            cvAnswerContainer.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_color)));
            tvAnswer.setTextColor(ContextCompat.getColor(this, R.color.text_color));
        }, 600);
    }
}