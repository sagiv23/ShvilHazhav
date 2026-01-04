package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MemoryGameLogAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class MemoryGameLogsTableActivity extends BaseActivity {
    private Button btnToAdminPage;
    private RecyclerView recyclerView;
    private MemoryGameLogAdapter adapter;

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

        btnToAdminPage = findViewById(R.id.btn_MemoryGameLogsTable_to_admin);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(MemoryGameLogsTableActivity.this, AdminPageActivity.class)));

        recyclerView = findViewById(R.id.recycler_MemoryGameLogsTable);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter = new MemoryGameLogAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        listenToGamesRealtime();
    }

    private void listenToGamesRealtime() {
        databaseService.getAllRoomsRealtime(new DatabaseService.DatabaseCallback<List<GameRoom>>() {
            @Override
            public void onCompleted(List<GameRoom> allRooms) {
                if (allRooms == null) return;

                // ניתן לבחור האם להציג את כל המשחקים או רק כאלו שהסתיימו
                // בדוגמה זו נציג את כולם כדי שתראה את השינויים בזמן אמת
                adapter.updateData(allRooms);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MemoryGameLogsTableActivity.this, "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}