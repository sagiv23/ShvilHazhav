package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ForumActivity extends AppCompatActivity {
    private Button btnToMain, btnToContact, btnToDetailsAboutUser, btnToExit, btnSendMessage;
    private EditText edtNewMessage;
    private RecyclerView recyclerForum;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private ForumAdapter adapter;
    private List<ForumMessage> messageList;
    private LinearLayoutManager layoutManager;
    private boolean userAtBottom = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("forum");

        PagePermissions.checkUserPage(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forumPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToMain = findViewById(R.id.btn_forum_main);
        btnToContact = findViewById(R.id.btn_forum_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_forum_DetailsAboutUser);
        btnToExit = findViewById(R.id.btn_forum_exit);
        btnSendMessage = findViewById(R.id.btn_forum_send_message);
        edtNewMessage = findViewById(R.id.edt_forum_new_message);
        recyclerForum = findViewById(R.id.recycler_forum);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(ForumActivity.this, DetailsAboutUserActivity.class)));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(ForumActivity.this));
        btnSendMessage.setOnClickListener(view -> sendMessage());

        messageList = new ArrayList<>();
        adapter = new ForumAdapter(messageList, mAuth);
        layoutManager = new LinearLayoutManager(this);
        recyclerForum.setLayoutManager(layoutManager);
        recyclerForum.setAdapter(adapter);

        recyclerForum.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition(), total = adapter.getItemCount();
                userAtBottom = (lastVisible == total - 1);
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        dbRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    ForumMessage msg = msgSnap.getValue(ForumMessage.class);
                    messageList.add(msg);
                }
                adapter.notifyDataSetChanged();

                if (userAtBottom || messageList.size() <= 2) {
                    recyclerForum.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void sendMessage() {
        String text = edtNewMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        User savedUser = SharedPreferencesUtil.getUser(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (savedUser == null || currentUser == null) {
            Toast.makeText(this, "שגיאה בזיהוי המשתמש", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        String fullName = savedUser.getFullName();
        long timestamp = System.currentTimeMillis();

        String messageId = dbRef.push().getKey();
        ForumMessage newMsg = new ForumMessage(messageId, fullName, email, text, timestamp, uid);

        dbRef.child(messageId).setValue(newMsg)
                .addOnSuccessListener(a -> edtNewMessage.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשליחת ההודעה", Toast.LENGTH_SHORT).show());
    }
}