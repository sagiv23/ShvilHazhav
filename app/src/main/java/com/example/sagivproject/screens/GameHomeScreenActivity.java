package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.LeaderboardAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IMemoryGameService;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The main screen for the memory game, allowing users to find opponents,
 * view their win count, and see a leaderboard of top players.
 * This activity handles the matchmaking process, transitioning to the
 * game activity once an opponent is found.
 */
@AndroidEntryPoint
public class GameHomeScreenActivity extends BaseActivity {
    private Button btnFindEnemy;
    private Button btnCancelFindEnemy;
    private Button btnSpeak;
    private TextView TVictories, tvGamesToday, tvGamesTotal, TVStatusOfFindingEnemy;
    private GameRoom currentRoom;
    private boolean gameStarted = false;
    private LeaderboardAdapter adapter;
    private User user;
    private TextToSpeech tts;
    private boolean isSpeaking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_home_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameHomeScreenPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = sharedPreferencesUtil.getUser();

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        btnFindEnemy = findViewById(R.id.btn_GameHomeScreen_find_enemy);
        btnCancelFindEnemy = findViewById(R.id.btn_GameHomeScreen_cancel_find_enemy);
        btnSpeak = findViewById(R.id.btn_GameHomeScreen_speak);
        TVictories = findViewById(R.id.tv_GameHomeScreen_victories);
        tvGamesToday = findViewById(R.id.tv_GameHomeScreen_games_today);
        tvGamesTotal = findViewById(R.id.tv_GameHomeScreen_games_total);
        TVStatusOfFindingEnemy = findViewById(R.id.tv_GameHomeScreen_status_of_finding_enemy);
        RecyclerView rvLeaderboard = findViewById(R.id.recyclerView_GameHomeScreen_leaderboard);

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("he", "IL"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "שפה לא נתמכת ב-TTS", Toast.LENGTH_SHORT).show();
                }

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> updateSpeakButton(true));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> updateSpeakButton(false));
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> updateSpeakButton(false));
                    }
                });
            }
        });

        btnFindEnemy.setOnClickListener(view -> findEnemy());
        btnCancelFindEnemy.setOnClickListener(view -> cancelSearch());
        btnSpeak.setOnClickListener(view -> toggleInstructionsSpeech());

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));

        adapter = adapterService.getLeaderboardAdapter();
        rvLeaderboard.setAdapter(adapter);
        setupLeaderboard();

        updateUI(SearchState.IDLE);
    }

    private void toggleInstructionsSpeech() {
        if (isSpeaking) {
            tts.stop();
            updateSpeakButton(false);
        } else {
            String sb = getString(R.string.game_rules) + ". " +
                    getString(R.string.game_rules_text1) + " " +
                    getString(R.string.game_target) + ". " +
                    getString(R.string.game_rules_text2) + " " +
                    getString(R.string.game_procedure) + ". " +
                    getString(R.string.game_rules_text3) + " " +
                    getString(R.string.game_rules_text4) + " " +
                    getString(R.string.game_rules_text5) + " " +
                    getString(R.string.scoring_method) + ". " +
                    getString(R.string.game_rules_text6);

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "instructions");
            tts.speak(sb, TextToSpeech.QUEUE_FLUSH, params, "instructions");
            updateSpeakButton(true);
        }
    }

    private void updateSpeakButton(boolean speaking) {
        isSpeaking = speaking;
        if (speaking) {
            btnSpeak.setText(R.string.cancel_playback);
        } else {
            btnSpeak.setText(R.string.playback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWins();
        gameStarted = false; // Reset game started flag when returning to screen
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) tts.stop();
        updateSpeakButton(false);
        if (currentRoom != null && !gameStarted) {
            cancelSearch();
        }
    }

    private void loadWins() {
        databaseService.getUserService().getUser(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    sharedPreferencesUtil.saveUser(updatedUser);
                    GameHomeScreenActivity.this.user = updatedUser;
                    displayTodayWins();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameHomeScreenActivity.this, "שגיאה בעדכון נתוני משתמש", Toast.LENGTH_SHORT).show();
                displayTodayWins();
            }
        });
    }

    private void displayTodayWins() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyStats stats = user.getDailyStats().get(today);

        int winsToday = (stats != null) ? stats.getMemoryWins() : 0;
        int gamesToday = (stats != null) ? stats.getMemoryGamesPlayed() : 0;

        TVictories.setText(MessageFormat.format("ניצחונות היום: {0}", winsToday));
        tvGamesToday.setText(MessageFormat.format("משחקים היום: {0}", gamesToday));

        int totalGames = 0;
        if (user.getDailyStats() != null) {
            for (DailyStats ds : user.getDailyStats().values()) {
                totalGames += ds.getMemoryGamesPlayed();
            }
        }
        tvGamesTotal.setText(MessageFormat.format("משחקים סך הכל: {0}", totalGames));
    }

    private void setupLeaderboard() {
        databaseService.getUserService().getUserList(new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) {
                    users.removeIf(User::isAdmin);
                    // Filter and sort by total wins (calculating from all daily stats)
                    users.sort((u1, u2) -> Integer.compare(getTotalWins(u2), getTotalWins(u1)));
                    users.removeIf(u -> getTotalWins(u) < 1);
                    adapter.setUsers(users);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameHomeScreenActivity.this, "שגיאה בטבלת המובילים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getTotalWins(User u) {
        int total = 0;
        if (u.getDailyStats() != null) {
            for (DailyStats ds : u.getDailyStats().values()) {
                total += ds.getMemoryWins();
            }
        }
        return total;
    }

    private void findEnemy() {
        updateUI(SearchState.SEARCHING);
        databaseService.getGameService().findOrCreateRoom(user, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(GameRoom room) {
                if (room == null) {
                    onFailed(new Exception("Room not created"));
                    return;
                }
                currentRoom = room;
                listenToRoom(room.getId());
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameHomeScreenActivity.this, "שגיאה במציאת יריב", Toast.LENGTH_SHORT).show();
                updateUI(SearchState.IDLE);
            }
        });
    }

    private void cancelSearch() {
        if (currentRoom != null) {
            databaseService.getGameService().removeRoomListener(currentRoom.getId());
        }

        if (currentRoom != null && "waiting".equals(currentRoom.getStatus()) && user.getId().equals(currentRoom.getPlayer1Uid())) {
            databaseService.getGameService().cancelRoom(currentRoom.getId(), null);
        }
        currentRoom = null;
        updateUI(SearchState.IDLE);
    }

    private void listenToRoom(String roomId) {
        if (roomId == null) {
            cancelSearch();
            return;
        }
        databaseService.getGameService().listenToRoomStatus(roomId, new IMemoryGameService.IRoomStatusCallback() {
            @Override
            public void onRoomStarted(GameRoom startedRoom) {
                if (gameStarted) return;
                gameStarted = true;
                updateUI(SearchState.GAME_FOUND);
                startGame(startedRoom);
            }

            @Override
            public void onRoomDeleted() {
                Toast.makeText(GameHomeScreenActivity.this, "החיפוש בוטל על ידי המארח.", Toast.LENGTH_SHORT).show();
                cancelSearch();
            }

            @Override
            public void onRoomFinished(GameRoom room) {
                cancelSearch();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameHomeScreenActivity.this, "אירעה שגיאה במעקב אחר החדר.", Toast.LENGTH_SHORT).show();
                cancelSearch();
            }
        });
    }

    private void startGame(GameRoom room) {
        if (currentRoom != null) {
            databaseService.getGameService().removeRoomListener(room.getId());
        }

        Intent intent = new Intent(this, MemoryGameActivity.class);
        intent.putExtra("roomId", room.getId());
        startActivity(intent);
        finish();
    }

    private void updateUI(SearchState state) {
        switch (state) {
            case IDLE:
                TVStatusOfFindingEnemy.setVisibility(View.GONE);
                btnCancelFindEnemy.setVisibility(View.GONE);
                btnFindEnemy.setVisibility(View.VISIBLE);
                break;
            case SEARCHING:
                TVStatusOfFindingEnemy.setText("מחפש יריב...");
                TVStatusOfFindingEnemy.setVisibility(View.VISIBLE);
                btnCancelFindEnemy.setVisibility(View.VISIBLE);
                btnFindEnemy.setVisibility(View.GONE);
                break;
            case GAME_FOUND:
                TVStatusOfFindingEnemy.setText("יריב נמצא! מתחיל משחק...");
                btnCancelFindEnemy.setVisibility(View.GONE);
                btnFindEnemy.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private enum SearchState {
        IDLE,
        SEARCHING,
        GAME_FOUND
    }
}
