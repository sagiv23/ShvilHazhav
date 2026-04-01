package com.example.sagivproject.screens;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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
 * Activity providing an administrative view of all historical and active memory game sessions.
 * <p>
 * This screen performs a real-time sync with the 'rooms' node in the database. It:
 * <ul>
 *     <li>Fetches all registered users to build a UID-to-Name resolution map.</li>
 *     <li>Displays a chronological log of game sessions using {@link MemoryGameLogAdapter}.</li>
 *     <li>Updates the log dynamically as game statuses (waiting, playing, finished) or scores change.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public class MemoryGameLogsTableActivity extends BaseActivity {
    /**
     * A local map used to resolve participant UIDs to human-readable names in the log.
     */
    private final Map<String, String> uidToNameMap = new HashMap<>();

    private MemoryGameLogAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_memory_game_logs_table);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.memoryGameLogsTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        RecyclerView recyclerView = findViewById(R.id.recycler_MemoryGameLogsTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = adapterService.getMemoryGameLogAdapter();
        recyclerView.setAdapter(adapter);

        fetchUsersAndListenToGames();
    }

    /**
     * Orchestrates the data loading sequence: first resolves names, then starts the game listener.
     */
    private void fetchUsersAndListenToGames() {
        databaseService.getUserService().getUserList(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> userList) {
                if (userList != null) {
                    for (User user : userList)
                        uidToNameMap.put(user.getId(), user.getFullName());
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
     * Establishes a persistent, real-time listener for all game room updates in the database.
     */
    private void listenToGamesRealtime() {
        databaseService.getGameService().getAllRoomsRealtime(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<GameRoom> allRooms) {
                if (allRooms != null)
                    adapter.submitData(allRooms, uidToNameMap);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MemoryGameLogsTableActivity.this, "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}