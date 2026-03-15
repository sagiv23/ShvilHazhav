package com.example.sagivproject.screens;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
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
 * A fragment for practicing various math problems to maintain cognitive health.
 * <p>
 * This fragment generates random arithmetic problems (addition, subtraction, multiplication,
 * division, powers, and square roots) and provides a custom numeric keypad for user input.
 * It tracks daily performance statistics (correct/wrong answers) and provides immediate
 * visual feedback to the user.
 * </p>
 */
@AndroidEntryPoint
public class MathProblemsFragment extends BaseFragment {
    private final StringBuilder userInput = new StringBuilder();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvCorrect, tvWrong;
    private User user;
    private TextView tvQuestion, tvAnswer;
    private MaterialCardView cvAnswerContainer;
    private int correctAnswer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_math_problems, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = sharedPreferencesUtil.getUser();

        tvCorrect = view.findViewById(R.id.tv_MathProblemsPage_correct);
        tvWrong = view.findViewById(R.id.tv_MathProblemsPage_wrong);
        tvQuestion = view.findViewById(R.id.tv_MathProblemsPage_question);
        tvAnswer = view.findViewById(R.id.tv_MathProblemsPage_user_answer);
        cvAnswerContainer = view.findViewById(R.id.cv_MathProblemsPage_answer_container);

        fetchLatestStats();
        generateProblem();
        setupKeypad(view);
    }

    /**
     * Fetches the latest user data from the database to ensure statistics are up-to-date.
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
     * Updates the UI text views with the user's correct and wrong answer counts for today.
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
     * Generates a new random math problem and updates the question display.
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
     * Helper to generate a random integer within a range.
     *
     * @param min Minimum value (inclusive).
     * @param max Maximum value (inclusive).
     * @return A random integer.
     */
    private int rand(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * Sets up click listeners for the custom numeric keypad.
     *
     * @param view The root view of the fragment.
     */
    private void setupKeypad(View view) {
        GridLayout keypad = view.findViewById(R.id.keypad_MathProblemsPage);
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
        view.findViewById(R.id.btn_MathProblemsPage_delete).setOnClickListener(v -> deleteLast());
        view.findViewById(R.id.btn_MathProblemsPage_clear).setOnClickListener(v -> clearInput());
        view.findViewById(R.id.btn_MathProblemsPage_submit).setOnClickListener(v -> checkAnswer());
    }

    /**
     * Deletes the last character from the user's input.
     */
    private void deleteLast() {
        if (userInput.length() > 0) {
            userInput.deleteCharAt(userInput.length() - 1);
            tvAnswer.setText(userInput.toString());
        }
    }

    /**
     * Clears all user input.
     */
    private void clearInput() {
        userInput.setLength(0);
        tvAnswer.setText("");
    }

    /**
     * Checks if the user's answer is correct, updates statistics, and provides feedback.
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
     * Retrieves the daily statistics for today, creating a new entry if it doesn't exist.
     *
     * @param date Today's date string.
     * @return The {@link DailyStats} object for today.
     */
    private DailyStats getTodayStats(String date) {
        if (user.getDailyStats() == null) user.setDailyStats(new HashMap<>());
        if (!user.getDailyStats().containsKey(date)) {
            user.getDailyStats().put(date, new DailyStats());
        }
        return user.getDailyStats().get(date);
    }

    /**
     * Shows visual feedback (color change) for correct or incorrect answers.
     *
     * @param isCorrect true if the answer was correct, false otherwise.
     */
    private void showFeedback(boolean isCorrect) {
        if (getContext() == null) return;
        int colorRes = isCorrect ? R.color.headline : R.color.error;
        int color = ContextCompat.getColor(getContext(), colorRes);
        cvAnswerContainer.setStrokeColor(ColorStateList.valueOf(color));
        tvAnswer.setTextColor(color);
        handler.postDelayed(() -> {
            if (getContext() != null) {
                cvAnswerContainer.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.text_color)));
                tvAnswer.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color));
            }
        }, 600);
    }
}
