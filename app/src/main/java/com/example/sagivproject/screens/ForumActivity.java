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

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ForumActivity extends AppCompatActivity {
    private Button btnToMain, btnToContact, btnToDetailsAboutUser, btnToExit, btnSendMessage;
    private EditText edtNewMessage;
    private RecyclerView recyclerForum;

    private FirebaseAuth mAuth;
    private ForumAdapter adapter;
    private List<ForumMessage> messageList;
    private LinearLayoutManager layoutManager;

    private boolean userAtBottom = true;

    private final DatabaseService db = DatabaseService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forumPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkUserPage(this);

        mAuth = FirebaseAuth.getInstance();

        btnToMain = findViewById(R.id.btn_forum_main);
        btnToContact = findViewById(R.id.btn_forum_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_forum_DetailsAboutUser);
        btnToExit = findViewById(R.id.btn_forum_exit);
        btnSendMessage = findViewById(R.id.btn_forum_send_message);
        edtNewMessage = findViewById(R.id.edt_forum_new_message);
        recyclerForum = findViewById(R.id.recycler_forum);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(this, DetailsAboutUserActivity.class)));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(this));

        btnSendMessage.setOnClickListener(view -> sendMessage());

        messageList = new ArrayList<>();
        adapter = new ForumAdapter(messageList, mAuth);
        layoutManager = new LinearLayoutManager(this);

        recyclerForum.setLayoutManager(layoutManager);
        recyclerForum.setAdapter(adapter);

        recyclerForum.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                userAtBottom = (lastVisible == adapter.getItemCount() - 1);
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        db.getForumMessagesRealtime(new DatabaseService.DatabaseCallback<List<ForumMessage>>() {
            @Override
            public void onCompleted(List<ForumMessage> list) {
                messageList.clear();
                messageList.addAll(list);
                adapter.notifyDataSetChanged();

                if (userAtBottom || messageList.size() <= 2) {
                    recyclerForum.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumActivity.this, "שגיאה בטעינת הודעות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = edtNewMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        User savedUser = SharedPreferencesUtil.getUser(this);
        if (savedUser == null) {
            Toast.makeText(this, "שגיאה בזיהוי המשתמש", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = db.generateForumMessageId();
        ForumMessage msg = new ForumMessage(messageId, savedUser.getFullName(), savedUser.getEmail(), text, System.currentTimeMillis(), savedUser.getUid());

        db.sendForumMessage(msg, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void obj) {
                edtNewMessage.setText("");
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumActivity.this, "שגיאה בשליחת ההודעה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
