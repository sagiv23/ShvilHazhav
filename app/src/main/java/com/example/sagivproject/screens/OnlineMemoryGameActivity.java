package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.PickAPic;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class OnlineMemoryGameActivity extends AppCompatActivity {
    private GridLayout gridMemoryBoard;
    private TextView tvGameTitle, tvTimer;
    private FirebaseFirestore db;
    private DocumentReference matchRef;
    private String uid, enemyUid, matchId;
    private boolean myTurn = false;
    private boolean isBusy = false;
    private boolean gameFinished = false;
    private boolean localLeftHandled = false;

    private List<Integer> cardList = new ArrayList<>();
    private ImageButton firstCard, secondCard;
    private int firstIndex = -1, secondIndex = -1;
    private int myScore = 0, enemyScore = 0;
    private int currentRound = 1, totalRounds = 3;

    private CountDownTimer roundTimer;
    private static final int ROUND_TIME_MS = 120000;

    private Handler uiHandler = new Handler();
    private com.google.firebase.firestore.ListenerRegistration matchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online_memory_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onlineMemoryGamePage), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() :
                getIntent().getStringExtra("myUid");
        matchId = getIntent().getStringExtra("gameRoomId");
        enemyUid = getIntent().getStringExtra("enemyUid");

        matchRef = db.collection("matches").document(matchId);

        gridMemoryBoard = findViewById(R.id.gridMemoryBoard);
        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvTimer = findViewById(R.id.tvMemoryTimer);

        setupMenuButton();
        initializeGame();
        listenToGameChanges();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leaveGameAndExit();
            }
        });
    }

    // 🟢 יציאה מסודרת – מעדכן את Firestore ושולח הודעה ליריב
    private void leaveGameAndExit() {
        if (gameFinished) {
            finish();
            return;
        }

        if (localLeftHandled) { finish(); return; }

        Map<String, Object> update = new HashMap<>();
        update.put("playerLeft", uid);
        update.put("finished", true);
        matchRef.update(update).addOnSuccessListener(aVoid -> {
            localLeftHandled = true;
            Toast.makeText(this, "עזבת את המשחק.", Toast.LENGTH_SHORT).show();
            if (roundTimer != null) roundTimer.cancel();
            gameFinished = true;
            uiHandler.postDelayed(() -> {
                startActivity(new Intent(this, GameHomeScreenActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }, 800);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה ביציאה מהמשחק", Toast.LENGTH_SHORT).show();
        });
    }

    // 🟡 הודעה לשחקן שנשאר כשהיריב עוזב
    private void showPlayerLeftDialog() {
        runOnUiThread(() -> {
            Toast.makeText(this, "היריב עזב את המשחק. ניצחת!", Toast.LENGTH_LONG).show();
            incrementUserWins(uid); // ✅ עדכון ניצחון
            matchRef.update(Map.of("finished", true, "winner", uid));
            uiHandler.postDelayed(() -> {
                startActivity(new Intent(this, GameHomeScreenActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }, 1500);
        });
    }

    // 🟢 התאמה דינמית של הקלפים לפי גודל המסך
    private void setupBoard() {
        gridMemoryBoard.removeAllViews();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int numCols = 3; // ✅ 3 עמודות קבועות
        int numRows = (int) Math.ceil(cardList.size() / (float) numCols);
        gridMemoryBoard.setColumnCount(numCols);

        int spacingPx = (int) dpToPx(8); // רווחים מעט גדולים יותר
        int availableWidth = screenWidth - spacingPx * (numCols + 1);
        int availableHeight = screenHeight - (int) dpToPx(320); // ✅ יותר מקום למעלה

        // ✅ מקטינים את הקלפים כך שלא ימלאו את כל המסך
        int cardWidth = (int) (availableWidth / (numCols + 0.3));
        int cardHeight = (int) (availableHeight / (numRows + 0.3));
        int cardSize = Math.min(cardWidth, cardHeight);

        for (int i = 0; i < cardList.size(); i++) {
            final int index = i;
            final ImageButton card = new ImageButton(this);
            card.setBackgroundResource(R.drawable.fold_card_img);
            card.setScaleType(ImageButton.ScaleType.CENTER_CROP);
            card.setAdjustViewBounds(true);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cardSize;
            params.height = cardSize;
            params.setMargins(spacingPx, spacingPx, spacingPx, spacingPx);
            card.setLayoutParams(params);

            card.setTag("faceDown");
            card.setOnClickListener(v -> onCardClicked(card, index));
            gridMemoryBoard.addView(card);
        }
    }

    private float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // 🟢 עדכון ניצחונות אמין – מוודא שהשדה קיים ומעלה ב־1
    private void incrementUserWins(String userUid) {
        db.collection("wins").document(userUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        doc.getReference().update("wins", FieldValue.increment(1));
                    } else {
                        db.collection("wins").document(userUid).set(Map.of("wins", 1));
                    }
                })
                .addOnFailureListener(e ->
                        db.collection("wins").document(userUid).set(Map.of("wins", 1))
                );
    }

    // 🟢 מאזין לשינויים בזמן אמת + טיפול בעזיבת שחקן
    private void listenToGameChanges() {
        matchListener = matchRef.addSnapshotListener((snapshot, e) -> {
            if (snapshot == null || e != null || !snapshot.exists()) return;

            String playerLeft = snapshot.getString("playerLeft");
            if (playerLeft != null && !localLeftHandled) {
                localLeftHandled = true;
                if (!playerLeft.equals(uid)) {
                    showPlayerLeftDialog();
                }
            }

            Boolean finished = snapshot.getBoolean("finished");
            if (Boolean.TRUE.equals(finished) && !gameFinished && playerLeft == null) {
                gameFinished = true;
                String winner = snapshot.getString("winner");
                runOnUiThread(() -> {
                    if (winner == null || winner.isEmpty()) {
                        Toast.makeText(this, "תיקו!", Toast.LENGTH_SHORT).show();
                    } else if (winner.equals(uid)) {
                        Toast.makeText(this, "ניצחת!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "הפסדת.", Toast.LENGTH_SHORT).show();
                    }
                    uiHandler.postDelayed(() -> {
                        startActivity(new Intent(this, GameHomeScreenActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }, 1500);
                });
            }
        });
    }

    // יצירת המשחק בהתחלה (או טעינה אם כבר קיים)
    private void initializeGame() {
        matchRef.get().addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) {
                // יצירת לוח ראשון
                PickAPic picker = new PickAPic(this, 20);
                List<Integer> selected = new ArrayList<>();
                while (selected.size() < 6) {
                    int img = picker.getRandomImageResId();
                    if (!selected.contains(img)) selected.add(img);
                }

                cardList.clear();
                for (int img : selected) {
                    cardList.add(img);
                    cardList.add(img);
                }
                Collections.shuffle(cardList);

                List<Long> toSave = new ArrayList<>();
                for (int v : cardList) toSave.add((long) v);

                myTurn = Math.random() < 0.5;

                Map<String, Object> data = new HashMap<>();
                data.put("board", toSave);
                data.put("turn", myTurn ? uid : enemyUid);
                data.put("scores", Map.of(uid, 0, enemyUid, 0));
                data.put("finished", false);
                data.put("winner", "");
                data.put("round", 1);
                data.put("matchedIndices", new ArrayList<Integer>());
                data.put("revealedIndices", new ArrayList<Integer>());
                data.put("leftGame", false);
                matchRef.set(data).addOnSuccessListener(aVoid -> {
                    setupBoard();
                    tvGameTitle.setText("סבב " + currentRound + " - " + (myTurn ? "התור שלך!" : "תור היריב..."));
                    startRoundTimer();
                });
            } else {
                // טען את הלוח הקיים (המרת Long->int)
                List<Object> boardObj = (List<Object>) doc.get("board");
                cardList.clear();
                if (boardObj != null) for (Object o : boardObj) cardList.add(((Number) o).intValue());

                String turnStr = doc.getString("turn");
                myTurn = turnStr != null && turnStr.equals(uid);

                Long roundNum = doc.getLong("round");
                if (roundNum != null) currentRound = roundNum.intValue();

                setupBoard();
                tvGameTitle.setText("סבב " + currentRound + " - " + (myTurn ? "התור שלך!" : "תור היריב..."));
                startRoundTimer();
            }
        });
    }

    // לחיצה על קלף
    private void onCardClicked(ImageButton card, int index) {
        if (!myTurn || isBusy || gameFinished) return;

        Object tag = card.getTag();
        if (tag != null && ("matched".equals(tag) || "revealed".equals(tag))) return;

        // הצגת תמונה
        int imageRes = cardList.get(index);
        card.setImageResource(imageRes);
        card.setTag("revealed");

        matchRef.update("revealedIndices", FieldValue.arrayUnion(index));

        if (firstIndex == -1) {
            firstIndex = index;
            firstCard = card;
        } else if (firstIndex == index) {
            // לחיצה על אותו קלף – לא עושים כלום
            return;
        } else {
            secondIndex = index;
            secondCard = card;
            isBusy = true;

            // ✅ נותנים רגע לצייר לפני שמעריכים את הזוג
            uiHandler.postDelayed(() -> evaluatePair(firstIndex, secondIndex), 400);
        }
    }

    // 🟢 הפעלת טיימר סבב
    private void startRoundTimer() {
        if (roundTimer != null) roundTimer.cancel();

        roundTimer = new CountDownTimer(ROUND_TIME_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("זמן: " + seconds + " שניות");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("הזמן נגמר!");
                myTurn = false;
                endRound();
            }
        }.start();
    }

    // 🟢 בדיקת זוג קלפים
    private void evaluatePair(int first, int second) {
        if (first == -1 || second == -1 || firstCard == null || secondCard == null) {
            isBusy = false;
            return;
        }

        boolean isMatch = Objects.equals(cardList.get(first), cardList.get(second));

        if (isMatch) {
            myScore++;
            firstCard.setTag("matched");
            secondCard.setTag("matched");

            matchRef.update("matchedIndices", FieldValue.arrayUnion(first, second));
            matchRef.update("scores." + uid, myScore);

            Toast.makeText(this, "התאמה נכונה!", Toast.LENGTH_SHORT).show();
            uiHandler.postDelayed(this::checkGameEnd, 600);

            // אם יש התאמה – השחקן ממשיך
            isBusy = false;
        } else {
            // ❗️אם אין התאמה – נהפוך חזרה אחרי השהייה ונעביר תור
            uiHandler.postDelayed(() -> {
                if (firstCard != null && secondCard != null) {
                    firstCard.setImageResource(R.drawable.fold_card_img);
                    secondCard.setImageResource(R.drawable.fold_card_img);
                    firstCard.setTag("faceDown");
                    secondCard.setTag("faceDown");
                }
                switchTurn();
                isBusy = false;
            }, 900);
        }

        // אפס משתנים
        firstCard = null;
        secondCard = null;
        firstIndex = -1;
        secondIndex = -1;
    }

    // 🟢 החלפת תור בין שחקנים
    private void switchTurn() {
        myTurn = !myTurn;
        matchRef.update("turn", myTurn ? uid : enemyUid);
        runOnUiThread(() -> tvGameTitle.setText("סבב " + currentRound + " - " + (myTurn ? "התור שלך!" : "תור היריב...")));
    }

    // 🟢 סיום סבב
    private void endRound() {
        myTurn = false;
        matchRef.update("turn", enemyUid);
        runOnUiThread(() -> tvGameTitle.setText("הזמן נגמר! תור היריב..."));
    }

    // 🟢 בדיקה אם כל הזוגות נחשפו — סיום משחק
    private void checkGameEnd() {
        // ✅ נבדוק לפי כמות הקלפים שסומנו כ־"matched" בפועל
        boolean allMatched = true;
        for (int i = 0; i < gridMemoryBoard.getChildCount(); i++) {
            View v = gridMemoryBoard.getChildAt(i);
            if (!"matched".equals(v.getTag())) {
                allMatched = false;
                break;
            }
        }

        if (!allMatched) return;

        gameFinished = true;
        String winner;
        if (myScore > enemyScore) winner = uid;
        else if (enemyScore > myScore) winner = enemyUid;
        else winner = "";

        Map<String, Object> result = new HashMap<>();
        result.put("finished", true);
        result.put("winner", winner);
        matchRef.update(result);

        if (winner.equals(uid)) {
            incrementUserWins(uid);
            Toast.makeText(this, "ניצחת!", Toast.LENGTH_SHORT).show();
        } else if (winner.isEmpty()) {
            Toast.makeText(this, "תיקו!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "הפסדת!", Toast.LENGTH_SHORT).show();
        }

        uiHandler.postDelayed(() -> {
            startActivity(new Intent(this, GameHomeScreenActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }, 1500);
    }

    private void setupMenuButton() {
        ImageButton btnMenu = findViewById(R.id.btnMemoryMenu);
        btnMenu.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View anchor) {
        View popupView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
        TextView text = popupView.findViewById(android.R.id.text1);
        text.setText("יציאה");

        PopupWindow popupWindow = new PopupWindow(popupView, (int) dpToPx(140), (int) dpToPx(56), true);
        text.setOnClickListener(v -> {
            popupWindow.dismiss();
            leaveGameAndExit();
        });

        popupWindow.showAsDropDown(anchor, 0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roundTimer != null) roundTimer.cancel();
        if (matchListener != null) matchListener.remove();
    }
}
