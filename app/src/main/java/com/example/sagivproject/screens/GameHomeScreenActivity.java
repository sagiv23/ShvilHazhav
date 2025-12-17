package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.database.ValueEventListener;

public class GameHomeScreenActivity extends AppCompatActivity {
    private Button btnToMain, btnToContact, btnToDetailsAboutUser,btnFindEnemy, btnCancelFindEnemy, btnToExit;
    private TextView TVictories, TVStatusOfFindingEnemy;
    private GameRoom currentRoom;
    private boolean gameStarted = false;
    private ValueEventListener roomListener;
    private User user;

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

        PagePermissions.checkUserPage(this);

        user = SharedPreferencesUtil.getUser(this);

        btnToMain = findViewById(R.id.btn_GameHomeScreen_to_main);
        btnToContact = findViewById(R.id.btn_GameHomeScreen_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_GameHomeScreen_to_DetailsAboutUser);
        btnFindEnemy = findViewById(R.id.btn_GameHomeScreen_find_enemy);
        btnCancelFindEnemy = findViewById(R.id.btn_GameHomeScreen_cancel_find_enemy);
        btnToExit = findViewById(R.id.btn_GameHomeScreen_to_exit);
        TVictories = findViewById(R.id.tv_GameHomeScreen_victories);
        TVStatusOfFindingEnemy = findViewById(R.id.tv_GameHomeScreen_status_of_finding_enemy);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, DetailsAboutUserActivity.class)));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(GameHomeScreenActivity.this));

        btnFindEnemy.setOnClickListener(view -> findEnemy());
        btnCancelFindEnemy.setOnClickListener(view -> cancel());
        loadWins(user);
    }

    private void loadWins(User user) {
        TVictories.setText("ניצחונות: " + user.getCountWins());
    }

    private void findEnemy() {
        TVStatusOfFindingEnemy.setVisibility(View.VISIBLE);
        btnCancelFindEnemy.setVisibility(View.VISIBLE);

        DatabaseService.getInstance().findOrCreateRoom(user,
                new DatabaseService.DatabaseCallback<GameRoom>() {
                    @Override
                    public void onCompleted(GameRoom room) {
                        currentRoom = room;
                        listenToRoom(room.getRoomId());
                    }

                    @Override
                    public void onFailed(Exception e) {
                    }
                }
        );
    }

    private void cancel() {
        if (currentRoom != null && "waiting".equals(currentRoom.getStatus()) && user.getUid().equals(currentRoom.getPlayer1().getUid())) {
            DatabaseService.getInstance().cancelRoom(currentRoom.getRoomId(), null);
            currentRoom = null;
        }

        TVStatusOfFindingEnemy.setVisibility(View.GONE);
        btnCancelFindEnemy.setVisibility(View.GONE);
    }

    private void listenToRoom(String roomId) {
        roomListener = DatabaseService.getInstance()
                .listenToRoomStatus(roomId,
                        new DatabaseService.RoomStatusCallback() {

                            @Override
                            public void onRoomStarted(GameRoom startedRoom) {
                                if (gameStarted) return;
                                gameStarted = true;
                                startGame(startedRoom);
                            }

                            @Override
                            public void onRoomDeleted() {
                                cancel();
                            }

                            @Override
                            public void onFailed(Exception e) {
                            }
                        }
                );
    }

    private void startGame(GameRoom room) {
        if (roomListener != null) {
            DatabaseService.getInstance()
                    .removeRoomListener(room.getRoomId(), roomListener);
            roomListener = null;
        }

        currentRoom = room;

        TVStatusOfFindingEnemy.setVisibility(View.GONE);
        btnCancelFindEnemy.setVisibility(View.GONE);

        Intent intent = new Intent(this, MemoryGameActivity.class);
        intent.putExtra("roomId", room.getRoomId());
        startActivity(intent);
        finish();
    }
}