package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.R;
import com.example.sagivproject.utils.PagePermissions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameHomeScreenActivity extends AppCompatActivity {
    Button btnToMain, btnToContact, btnToDetailsAboutUser, btnFindEnemy, btnCancelFindEnemy, btnToExit;
    TextView TVStatusOfFindingEnemy;
    private ListenerRegistration listenerRegistration;
    private ListenerRegistration winsListener;
    private boolean searching = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_home_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameHomeScreenPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkUserPage(this);

        btnToMain = findViewById(R.id.btn_GameHomeScreen_to_main);
        btnToContact = findViewById(R.id.btn_GameHomeScreen_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_GameHomeScreen_to_DetailsAboutUser);
        btnFindEnemy = findViewById(R.id.btn_GameHomeScreen_find_enemy);
        btnCancelFindEnemy = findViewById(R.id.btn_GameHomeScreen_cancel_find_enemy);
        btnToExit = findViewById(R.id.btn_GameHomeScreen_to_exit);
        TVStatusOfFindingEnemy = findViewById(R.id.tv_GameHomeScreen_status_of_finding_enemy);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, DetailsAboutUserActivity.class)));
        btnFindEnemy.setOnClickListener(view -> startSearching());
        btnCancelFindEnemy.setOnClickListener(view -> cancelSearching());
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(GameHomeScreenActivity.this));

        loadUserWins();
    }

    private void loadUserWins() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TextView winsText = findViewById(R.id.tv_GameHomeScreen_victories);

        winsListener = db.collection("wins").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        Long wins = snapshot.getLong("wins");
                        winsText.setText("ניצחונות: " + (wins != null ? wins : 0));
                    } else {
                        winsText.setText("ניצחונות: 0");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
        if (winsListener != null) winsListener.remove();
        cancelSearching();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (searching) cancelSearching();
    }

    private void startSearching() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        searching = true;
        btnFindEnemy.setVisibility(View.GONE);
        btnCancelFindEnemy.setVisibility(View.VISIBLE);
        TVStatusOfFindingEnemy.setText("מחפש יריב...");

        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        CollectionReference searchingRef = db.collection("searchingPlayers");

        searchingRef.whereEqualTo("matched", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    DocumentSnapshot enemyDoc = null;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String enemyUid = doc.getString("uid");
                        if (enemyUid != null && !enemyUid.equals(uid)) {
                            enemyDoc = doc;
                            break;
                        }
                    }

                    if (enemyDoc != null) {
                        String enemyUid = enemyDoc.getString("uid");
                        String enemyName = enemyDoc.getString("fullName");
                        String gameRoomId = UUID.randomUUID().toString();

                        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                            String myFullName = "שחקן לא ידוע";
                            if (userDoc.exists()) {
                                String first = userDoc.getString("firstName");
                                String last = userDoc.getString("lastName");
                                if (first != null && last != null)
                                    myFullName = first + " " + last;
                            }

                            Map<String, Object> myData = makePlayerData(uid, email, myFullName);
                            myData.put("matched", true);
                            myData.put("enemyUid", enemyUid);
                            myData.put("enemyName", enemyName);
                            myData.put("gameRoomId", gameRoomId);
                            searchingRef.document(uid).set(myData);

                            searchingRef.document(enemyUid).update("matched", true,
                                    "enemyUid", uid,
                                    "enemyName", myFullName,
                                    "gameRoomId", gameRoomId);

                            TVStatusOfFindingEnemy.setText("יריב נמצא! " + enemyName);
                            btnCancelFindEnemy.setVisibility(View.GONE);

                            ensureWinRecord(uid);
                            ensureWinRecord(enemyUid);

                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(GameHomeScreenActivity.this, OnlineMemoryGameActivity.class);
                                intent.putExtra("gameRoomId", gameRoomId);
                                intent.putExtra("myUid", uid);
                                intent.putExtra("enemyUid", enemyUid);
                                intent.putExtra("isPlayer1", true);
                                startActivity(intent);
                                finish();
                            }, 3000);
                        });

                    } else {
                        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                            String fullName = "שחקן לא ידוע";
                            if (userDoc.exists()) {
                                String first = userDoc.getString("firstName");
                                String last = userDoc.getString("lastName");
                                if (first != null && last != null)
                                    fullName = first + " " + last;
                            }

                            searchingRef.document(uid).set(makePlayerData(uid, email, fullName))
                                    .addOnSuccessListener(a -> listenForMatch(uid));
                        });
                    }
                });
    }

    private void listenForMatch(String uid) {
        listenerRegistration = db.collection("searchingPlayers").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        Boolean matched = snapshot.getBoolean("matched");
                        if (matched != null && matched) {
                            String enemyName = snapshot.getString("enemyName");
                            String enemyUid = snapshot.getString("enemyUid");
                            String gameRoomId = snapshot.getString("gameRoomId");

                            TVStatusOfFindingEnemy.setText("יריב נמצא! " + enemyName);
                            btnCancelFindEnemy.setVisibility(View.GONE);
                            ensureWinRecord(uid);
                            ensureWinRecord(enemyUid);

                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(GameHomeScreenActivity.this, OnlineMemoryGameActivity.class);
                                intent.putExtra("gameRoomId", gameRoomId);
                                intent.putExtra("myUid", uid);
                                intent.putExtra("enemyUid", enemyUid);
                                intent.putExtra("isPlayer1", false);
                                startActivity(intent);
                                finish();
                            }, 3000);
                        }
                    }
                });
    }

    private Map<String, Object> makePlayerData(String uid, String email, String fullName) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("email", email);
        data.put("fullName", fullName);
        data.put("timestamp", System.currentTimeMillis());
        data.put("matched", false);
        data.put("enemyUid", null);
        data.put("enemyName", null);
        data.put("gameRoomId", null);
        return data;
    }

    private void ensureWinRecord(String uid) {
        db.collection("wins").document(uid).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                db.collection("wins").document(uid).set(Map.of("wins", 0));
            }
        });
    }

    private void cancelSearching() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("searchingPlayers").document(user.getUid()).delete();
        }
        searching = false;
        btnFindEnemy.setVisibility(View.VISIBLE);
        btnCancelFindEnemy.setVisibility(View.GONE);
        TVStatusOfFindingEnemy.setText("");
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}
