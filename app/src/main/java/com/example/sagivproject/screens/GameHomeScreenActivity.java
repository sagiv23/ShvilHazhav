package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.LeaderboardAdapter;
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
 * The home screen for the memory game, providing matchmaking and user statistics.
 * <p>
 * This activity serves as the lobby for the online memory game. It allows users to:
 * <ul>
 * <li>View personal game statistics (wins today, games played).</li>
 * <li>See a global leaderboard of top-performing players.</li>
 * <li>Find or create a game room to play against a live opponent.</li>
 * <li>Listen to the game rules via Text-to-Speech (TTS).</li>
 * </ul>
 * It manages the matchmaking lifecycle and transitions the user to the active game screen.
 * </p>
 */
@AndroidEntryPoint
public class GameHomeScreenActivity extends BaseActivity {
    private Button btnFindEnemy, btnCancelFindEnemy, btnSpeak;
    private TextView TVStatusOfFindingEnemy;
    private GameRoom currentRoom;
    private boolean gameStarted = false;
    private LeaderboardAdapter adapter;
    private User user;
    private TextToSpeech tts;
    private boolean isSpeaking = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_game_home_screen, R.id.gameHomeScreenPage);
        setupMenu();

        user = sharedPreferencesUtil.getUser();

        btnFindEnemy = findViewById(R.id.btn_GameHomeScreen_find_enemy);
        btnCancelFindEnemy = findViewById(R.id.btn_GameHomeScreen_cancel_find_enemy);
        btnSpeak = findViewById(R.id.btn_GameHomeScreen_speak);
        TVStatusOfFindingEnemy = findViewById(R.id.tv_GameHomeScreen_status_of_finding_enemy);
        RecyclerView rvLeaderboard = findViewById(R.id.recyclerView_GameHomeScreen_leaderboard);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
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

        btnFindEnemy.setOnClickListener(v -> findEnemy());
        btnCancelFindEnemy.setOnClickListener(v -> cancelSearch());
        btnSpeak.setOnClickListener(v -> toggleInstructionsSpeech());

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = adapterService.getLeaderboardAdapter();
        rvLeaderboard.setAdapter(adapter);
        setupLeaderboard();

        updateUI(SearchState.IDLE);
    }

    /**
     * Toggles the playback of game rules using the TTS engine.
     */
    private void toggleInstructionsSpeech() {
        if (isSpeaking) {
            tts.stop();
            updateSpeakButton(false);
        } else {
            String rulesText = getString(R.string.game_rules) + ". " +
                    getString(R.string.game_rules_text1) + " " +
                    getString(R.string.game_target) + ". " +
                    getString(R.string.game_rules_text2) + " " +
                    getString(R.string.game_procedure) + ". " +
                    getString(R.string.game_rules_text3) + " " +
                    getString(R.string.game_rules_text4) + " " +
                    getString(R.string.game_rules_text5) + " " +
                    getString(R.string.scoring_method) + ". " +
                    getString(R.string.game_rules_text6);
            tts.speak(rulesText, TextToSpeech.QUEUE_FLUSH, null, "instructions");
            updateSpeakButton(true);
        }
    }

    /**
     * Updates the speak button text based on the TTS status.
     *
     * @param speaking true if TTS is active.
     */
    private void updateSpeakButton(boolean speaking) {
        isSpeaking = speaking;
        btnSpeak.setText(speaking ? R.string.cancel_playback : R.string.playback);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayTodayWins();
        loadWins();
        gameStarted = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tts != null) tts.stop();
        if (currentRoom != null && !gameStarted) cancelSearch();
        updateSpeakButton(false);
    }

    /**
     * Fetches current user data from the database to refresh win/game counts.
     */
    private void loadWins() {
        databaseService.getUserService().getUser(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    sharedPreferencesUtil.saveUser(updatedUser);
                    user = updatedUser;
                }
            }

            @Override
            public void onFailed(Exception e) {
                displayTodayWins();
            }
        });
    }

    /**
     * Calculates and displays today's statistics in the UI.
     */
    private void displayTodayWins() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyStats stats = user.getDailyStats().get(today);
        int winsToday = (stats != null) ? stats.getMemoryWins() : 0;
        int gamesToday = (stats != null) ? stats.getMemoryGamesPlayed() : 0;
        int totalGames = getTotalGamesPlayed(user);

        ((TextView) findViewById(R.id.tv_GameHomeScreen_victories)).setText(MessageFormat.format("ניצחונות היום: {0}", winsToday));
        ((TextView) findViewById(R.id.tv_GameHomeScreen_games_today)).setText(MessageFormat.format("משחקים היום: {0}", gamesToday));
        ((TextView) findViewById(R.id.tv_GameHomeScreen_games_total)).setText(MessageFormat.format("משחקים סך הכל: {0}", totalGames));
    }

    /**
     * Fetches all users and populates the leaderboard adapter, sorted by total wins.
     */
    private void setupLeaderboard() {
        databaseService.getUserService().getUserList(new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users != null) {
                    users.removeIf(User::isAdmin);
                    users.sort((u1, u2) -> Integer.compare(getTotalWins(u2), getTotalWins(u1)));
                    users.removeIf(u -> getTotalWins(u) < 1);
                    adapter.setUsers(users);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameHomeScreenActivity.this, "שגיאה בטעינת המובילים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper to calculate cumulative wins for a user across all days.
     */
    private int getTotalWins(User u) {
        int total = 0;
        if (u.getDailyStats() != null) {
            for (DailyStats ds : u.getDailyStats().values()) {
                total += ds.getMemoryWins();
            }
        }
        return total;
    }

    /**
     * Helper to calculate total memory games played for a user across all days.
     */
    private int getTotalGamesPlayed(User u) {
        int total = 0;
        if (u.getDailyStats() != null) {
            for (DailyStats ds : u.getDailyStats().values()) {
                total += ds.getMemoryGamesPlayed();
            }
        }
        return total;
    }

    /**
     * Initiates the matchmaking process via the game service.
     */
    private void findEnemy() {
        updateUI(SearchState.SEARCHING);
        databaseService.getGameService().findOrCreateRoom(user, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(GameRoom room) {
                if (room != null) {
                    currentRoom = room;
                    listenToRoom(room.getId());
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(GameHomeScreenActivity.this, "שגיאה במציאת יריב", Toast.LENGTH_SHORT).show();
                updateUI(SearchState.IDLE);
            }
        });
    }

    /**
     * Cancels an active search and cleans up the pending room in the database.
     */
    private void cancelSearch() {
        if (currentRoom != null) {
            databaseService.getGameService().removeRoomListener(currentRoom.getId());
            if ("waiting".equals(currentRoom.getStatus()) && user.getId().equals(currentRoom.getPlayer1Uid()))
                databaseService.getGameService().cancelRoom(currentRoom.getId(), null);
        }
        currentRoom = null;
        updateUI(SearchState.IDLE);
    }

    /**
     * Monitors the status of the assigned game room for state changes.
     *
     * @param roomId The unique ID of the room to listen to.
     */
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

                Intent intent = new Intent(GameHomeScreenActivity.this, MemoryGameActivity.class);
                intent.putExtra("roomId", startedRoom.getId());
                startActivity(intent);
            }

            @Override
            public void onRoomDeleted() {
                Toast.makeText(GameHomeScreenActivity.this, "החיפוש בוטל.", Toast.LENGTH_SHORT).show();
                cancelSearch();
            }

            @Override
            public void onRoomFinished(GameRoom room) {
                cancelSearch();
            }

            @Override
            public void onFailed(Exception e) {
                cancelSearch();
            }
        });
    }

    /**
     * Updates UI component visibility and text based on the matchmaking state.
     *
     * @param state The current search state.
     */
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
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Enumerates the possible states of the matchmaking search process.
     */
    private enum SearchState {
        IDLE,
        SEARCHING,
        GAME_FOUND
    }
}