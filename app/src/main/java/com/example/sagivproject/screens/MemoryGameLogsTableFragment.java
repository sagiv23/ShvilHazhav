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
 * An admin fragment to display a real-time log of all memory game rooms.
 */
@AndroidEntryPoint
public class MemoryGameLogsTableFragment extends BaseFragment {
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
