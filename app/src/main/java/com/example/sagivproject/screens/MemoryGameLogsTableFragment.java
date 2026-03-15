package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MemoryGameLogAdapter;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An admin-facing fragment that displays a real-time log of all memory game rooms.
 * <p>
 * This fragment fetches all users to create a UID-to-Name mapping and then
 * listens for real-time updates to all game rooms in the database. It uses
 * the {@link MemoryGameLogAdapter} to display the history and status of these games.
 * </p>
 */
@AndroidEntryPoint
public class MemoryGameLogsTableFragment extends BaseFragment {
    /**
     * Map used to resolve user UIDs to full names for display in the log.
     */
    private final Map<String, String> uidToNameMap = new HashMap<>();
    private MemoryGameLogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memory_game_logs_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_MemoryGameLogsTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = adapterService.getMemoryGameLogAdapter();
        recyclerView.setAdapter(adapter);

        fetchUsersAndListenToGames();
    }

    /**
     * Fetches the user list from the database to populate the name map,
     * and then starts listening to the game rooms.
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
                Toast.makeText(requireContext(), "שגיאה בטעינת שמות המשתמשים", Toast.LENGTH_SHORT).show();
                listenToGamesRealtime();
            }
        });
    }

    /**
     * Sets up a real-time listener for all game rooms in the database.
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
                Toast.makeText(requireContext(), "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
