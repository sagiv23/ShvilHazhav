package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MemoryGameLogAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An admin activity to display a real-time log of all memory game rooms.
 * <p>
 * This screen provides a live-updating list of all game rooms (past and present),
 * showing details such as the players involved, scores, and game status.
 * It is intended for administrative or debugging purposes.
 * </p>
 */
@AndroidEntryPoint
public class MemoryGameLogsTableActivity extends BaseActivity {
    private final Map<String, String> uidToNameMap = new HashMap<>();
    private MemoryGameLogAdapter adapter;

    /**
     * Initializes the activity, sets up the UI and RecyclerView, and starts listening
     * for real-time updates to the game rooms.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_memory_game_logs_table);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.memoryGameLogsTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        RecyclerView recyclerView = findViewById(R.id.recycler_MemoryGameLogsTable);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter = new MemoryGameLogAdapter(uidToNameMap);
        recyclerView.setAdapter(adapter);

        fetchUsersAndListenToGames();
    }

    /**
     * Fetches the list of all users to map UIDs to full names, then starts listening
     * for real-time updates to game rooms.
     */
    private void fetchUsersAndListenToGames() {
        databaseService.getUserService().getUserList(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> userList) {
                if (userList != null) {
                    for (User user : userList) {
                        uidToNameMap.put(user.getId(), user.getFullName());
                    }
                }
                listenToGamesRealtime();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MemoryGameLogsTableActivity.this, "שגיאה בטעינת שמות המשתמשים", Toast.LENGTH_SHORT).show();
                listenToGamesRealtime();
            }
        });
    }

    /**
     * Sets up a real-time listener on the database to fetch and display all game rooms.
     * The list updates automatically as game data changes.
     */
    private void listenToGamesRealtime() {
        databaseService.getGameService().getAllRoomsRealtime(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<GameRoom> allRooms) {
                if (allRooms == null) return;
                adapter.submitData(allRooms, uidToNameMap);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MemoryGameLogsTableActivity.this, "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
