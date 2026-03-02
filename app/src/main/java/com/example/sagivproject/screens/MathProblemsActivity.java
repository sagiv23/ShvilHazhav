package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.Operation;

import java.text.MessageFormat;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity for practicing various math problems.
 * <p>
 * This screen generates random math problems (addition, subtraction, multiplication, division,
 * powers, and square roots) and allows the user to solve them using a numeric keypad.
 * It tracks and displays the number of correct and incorrect answers and allows the user
 * to reset their statistics.
 * </p>
 */
@AndroidEntryPoint
public class MathProblemsActivity extends BaseActivity {
    private final StringBuilder userInput = new StringBuilder();
    private TextView tvCorrect, tvWrong;
    private User user;
    private TextView tvQuestion, tvAnswer;
    private int correctAnswer;

    /**
     * Initializes the activity, sets up the UI, generates the first problem,
     * and configures the keypad.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_math_problems);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mathProblemsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = sharedPreferencesUtil.getUser();

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        tvCorrect = findViewById(R.id.tv_MathProblemsPage_correct);
        tvWrong = findViewById(R.id.tv_MathProblemsPage_wrong);
        Button btnResetStats = findViewById(R.id.btn_MathProblemsPage_resetStats);
        tvQuestion = findViewById(R.id.tv_MathProblemsPage_question);
        tvAnswer = findViewById(R.id.tv_MathProblemsPage_user_answer);

        btnResetStats.setOnClickListener(v -> dialogService.showConfirmDialog("איפוס נתונים", "האם לאפס את הנתונים?", "אפס", "בטל", this::resetStats));

        generateProblem();
        setupKeypad();
    }

    /**
     * Updates the statistics UI when the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateStatsUI();
    }

    /**
     * Updates the text views for correct and wrong answers with the current user stats.
     */
    private void updateStatsUI() {
        tvCorrect.setText(MessageFormat.format("נכונות: {0}", user.getMathProblemsStats().getCorrectAnswers()));
        tvWrong.setText(MessageFormat.format("טעויות: {0}", user.getMathProblemsStats().getWrongAnswers()));
    }

    /**
     * Resets the user's math problem statistics (correct and wrong answers) to zero,
     * both locally and in the database.
     */
    private void resetStats() {
        user.getMathProblemsStats().setCorrectAnswers(0);
        user.getMathProblemsStats().setWrongAnswers(0);

        databaseService.getStatsService().resetMathStats(user.getId());
        sharedPreferencesUtil.saveUser(user);

        updateStatsUI();
        Toast.makeText(this, "הנתונים אופסו בהצלחה", Toast.LENGTH_SHORT).show();
    }

    /**
     * Generates a new random math problem, including the operation and operands,
     * calculates the correct answer, and displays the question in the UI.
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
     * Generates a random integer within a specified range (inclusive).
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A random integer between min and max.
     */
    private int rand(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * Sets up the on-click listeners for the numeric keypad buttons and control buttons
     * (delete, clear, submit).
     */
    private void setupKeypad() {
        GridLayout keypad = findViewById(R.id.keypad_MathProblemsPage);

        for (int i = 0; i < keypad.getChildCount(); i++) {
            View v = keypad.getChildAt(i);

            if (v instanceof Button) {
                Button btn = (Button) v;
                String text = btn.getText().toString();

                if (text.matches("\\d+")) {
                    btn.setOnClickListener(view -> {
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
     * Deletes the last character from the user's input.
     */
    private void deleteLast() {
        if (userInput.length() > 0) {
            userInput.deleteCharAt(userInput.length() - 1);
            tvAnswer.setText(userInput.toString());
        }
    }

    /**
     * Clears the user's entire input.
     */
    private void clearInput() {
        userInput.setLength(0);
        tvAnswer.setText("");
    }

    /**
     * Checks the user's submitted answer against the correct answer. Updates statistics,
     * provides feedback via a Toast message, and generates a new problem if correct.
     */
    private void checkAnswer() {
        if (userInput.length() == 0) return;

        int userAnswer = Integer.parseInt(userInput.toString());

        if (userAnswer == correctAnswer) {
            user.getMathProblemsStats().setCorrectAnswers(user.getMathProblemsStats().getCorrectAnswers() + 1);

            Toast.makeText(this, "נכון! ✅", Toast.LENGTH_SHORT).show();
            generateProblem();
            databaseService.getStatsService().addCorrectAnswer(user.getId());
        } else {
            user.getMathProblemsStats().setWrongAnswers(user.getMathProblemsStats().getWrongAnswers() + 1);

            Toast.makeText(this, "טעות, נסה שוב ❌", Toast.LENGTH_SHORT).show();
            databaseService.getStatsService().addWrongAnswer(user.getId());
        }

        sharedPreferencesUtil.saveUser(user);

        updateStatsUI();
    }
}
