package com.example.sagivproject.screens;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.MathProblemsStats;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.Operation;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.google.android.material.card.MaterialCardView;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity for practicing various math problems.
 */
@AndroidEntryPoint
public class MathProblemsActivity extends BaseActivity {
    private final StringBuilder userInput = new StringBuilder();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvCorrect, tvWrong;
    private User user;
    private TextView tvQuestion, tvAnswer;
    private MaterialCardView cvAnswerContainer;
    private int correctAnswer;

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
        tvQuestion = findViewById(R.id.tv_MathProblemsPage_question);
        tvAnswer = findViewById(R.id.tv_MathProblemsPage_user_answer);
        cvAnswerContainer = findViewById(R.id.cv_MathProblemsPage_answer_container);

        fetchLatestStatsAndCheckReset();
        generateProblem();
        setupKeypad();
    }

    private void fetchLatestStatsAndCheckReset() {
        databaseService.getUserService().getUser(user.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    user = updatedUser;
                    sharedPreferencesUtil.saveUser(user);
                }
                checkDailyReset();
            }

            @Override
            public void onFailed(Exception e) {
                checkDailyReset();
            }
        });
    }

    private void checkDailyReset() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        MathProblemsStats stats = user.getMathProblemsStats();

        if (!today.equals(stats.getLastUpdateDate())) {
            stats.setCorrectAnswers(0);
            stats.setWrongAnswers(0);
            stats.setLastUpdateDate(today);

            databaseService.getStatsService().resetMathStats(user.getId(), today);
            sharedPreferencesUtil.saveUser(user);
        }
        updateStatsUI();
    }

    private void updateStatsUI() {
        tvCorrect.setText(MessageFormat.format("נכונות: {0}", user.getMathProblemsStats().getCorrectAnswers()));
        tvWrong.setText(MessageFormat.format("טעויות: {0}", user.getMathProblemsStats().getWrongAnswers()));
    }

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

    private int rand(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

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

    private void deleteLast() {
        if (userInput.length() > 0) {
            userInput.deleteCharAt(userInput.length() - 1);
            tvAnswer.setText(userInput.toString());
        }
    }

    private void clearInput() {
        userInput.setLength(0);
        tvAnswer.setText("");
    }

    private void checkAnswer() {
        if (userInput.length() == 0) return;
        int userAnswer = Integer.parseInt(userInput.toString());

        if (userAnswer == correctAnswer) {
            user.getMathProblemsStats().setCorrectAnswers(user.getMathProblemsStats().getCorrectAnswers() + 1);
            showFeedback(true);
            generateProblem();
            databaseService.getStatsService().addCorrectAnswer(user.getId());
            databaseService.getStatsService().updateDailyMathStats(user.getId(), true);
        } else {
            user.getMathProblemsStats().setWrongAnswers(user.getMathProblemsStats().getWrongAnswers() + 1);
            showFeedback(false);
            databaseService.getStatsService().addWrongAnswer(user.getId());
            databaseService.getStatsService().updateDailyMathStats(user.getId(), false);
        }

        sharedPreferencesUtil.saveUser(user);
        updateStatsUI();
    }

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
