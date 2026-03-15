package com.example.sagivproject.screens;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.LeaderboardAdapter;
import com.example.sagivproject.bases.BaseFragment;
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
 * The home screen for the memory game, providing matchmaking and statistics.
 * <p>
 * This fragment allows users to view their game statistics (wins/plays), see a global leaderboard,
 * and find an opponent for an online 1-on-1 match. It also includes an accessibility feature
 * to read the game rules aloud using Text-to-Speech.
 * </p>
 */
@AndroidEntryPoint
public class GameHomeScreenFragment extends BaseFragment {
    private Button btnFindEnemy, btnCancelFindEnemy, btnSpeak;
    private TextView TVictories, tvGamesToday, tvGamesTotal, TVStatusOfFindingEnemy;
    private GameRoom currentRoom;
    private boolean gameStarted = false;
    private LeaderboardAdapter adapter;
    private User user;
    private TextToSpeech tts;
    private boolean isSpeaking = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_home_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = sharedPreferencesUtil.getUser();

        btnFindEnemy = view.findViewById(R.id.btn_GameHomeScreen_find_enemy);
        btnCancelFindEnemy = view.findViewById(R.id.btn_GameHomeScreen_cancel_find_enemy);
        btnSpeak = view.findViewById(R.id.btn_GameHomeScreen_speak);
        TVictories = view.findViewById(R.id.tv_GameHomeScreen_victories);
        tvGamesToday = view.findViewById(R.id.tv_GameHomeScreen_games_today);
        tvGamesTotal = view.findViewById(R.id.tv_GameHomeScreen_games_total);
        TVStatusOfFindingEnemy = view.findViewById(R.id.tv_GameHomeScreen_status_of_finding_enemy);
        RecyclerView rvLeaderboard = view.findViewById(R.id.recyclerView_GameHomeScreen_leaderboard);

        // Initialize TTS for game instructions
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(true));
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(false));
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> updateSpeakButton(false));
                    }
                });
            }
        });

        btnFindEnemy.setOnClickListener(v -> findEnemy());
        btnCancelFindEnemy.setOnClickListener(v -> cancelSearch());
        btnSpeak.setOnClickListener(v -> toggleInstructionsSpeech());

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = adapterService.getLeaderboardAdapter();
        rvLeaderboard.setAdapter(adapter);
        setupLeaderboard();

        updateUI(SearchState.IDLE);
    }

    /**
     * Toggles the playback of game instructions using Text-to-Speech.
     */
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
            tts.speak(sb, TextToSpeech.QUEUE_FLUSH, null, "instructions");
            updateSpeakButton(true);
        }
    }

    /**
     * Updates the UI state of the speech button.
     *
     * @param speaking true if instructions are being read, false otherwise.
     */
    private void updateSpeakButton(boolean speaking) {
        isSpeaking = speaking;
        btnSpeak.setText(speaking ? R.string.cancel_playback : R.string.playback);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWins();
        gameStarted = false; // Reset game started flag when returning to screen
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tts != null) tts.stop();
        if (currentRoom != null && !gameStarted) cancelSearch();
        updateSpeakButton(false);
    }

    /**
     * Fetches the latest win statistics for the current user.
     */
    private void loadWins() {
        databaseService.getUserService().getUser(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser != null) {
                    sharedPreferencesUtil.saveUser(updatedUser);
                    user = updatedUser;
                    displayTodayWins();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה בעדכון נתוני משתמש", Toast.LENGTH_SHORT).show();
                displayTodayWins();
            }
        });
    }

    /**
     * Displays the calculated win statistics in the UI.
     */
    private void displayTodayWins() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyStats stats = user.getDailyStats().get(today);
        int winsToday = (stats != null) ? stats.getMemoryWins() : 0;
        int gamesToday = (stats != null) ? stats.getMemoryGamesPlayed() : 0;
        int totalGames = getTotalWins(user);

        TVictories.setText(MessageFormat.format("ניצחונות היום: {0}", winsToday));
        tvGamesToday.setText(MessageFormat.format("משחקים היום: {0}", gamesToday));
        tvGamesTotal.setText(MessageFormat.format("משחקים סך הכל: {0}", totalGames));
    }

    /**
     * Fetches and displays the top players in the global leaderboard.
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
                Toast.makeText(requireContext(), "שגיאה בטבלת המובילים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper to calculate the total wins for a user across all days.
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
     * Initiates the matchmaking process to find an opponent.
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
                Toast.makeText(requireContext(), "שגיאה במציאת יריב", Toast.LENGTH_SHORT).show();
                updateUI(SearchState.IDLE);
            }
        });
    }

    /**
     * Cancels the current search for an opponent and cleans up the room if necessary.
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
     * Listens for status changes in the current game room.
     *
     * @param roomId The ID of the room to monitor.
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

                // Using Safe Args Directions to navigate to the game screen
                GameHomeScreenFragmentDirections.ActionGameHomeScreenFragmentToMemoryGameFragment action =
                        GameHomeScreenFragmentDirections.actionGameHomeScreenFragmentToMemoryGameFragment(startedRoom.getId());
                navigateTo(action);
            }

            @Override
            public void onRoomDeleted() {
                Toast.makeText(requireContext(), "החיפוש בוטל על ידי המארח.", Toast.LENGTH_SHORT).show();
                cancelSearch();
            }

            @Override
            public void onRoomFinished(GameRoom room) {
                cancelSearch();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "אירעה שגיאה במעקב אחר החדר.", Toast.LENGTH_SHORT).show();
                cancelSearch();
            }
        });
    }

    /**
     * Updates the UI components based on the current matchmaking search state.
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
     * Enum defining the possible states of the matchmaking process.
     */
    private enum SearchState {
        IDLE,
        SEARCHING,
        GAME_FOUND
    }
}
