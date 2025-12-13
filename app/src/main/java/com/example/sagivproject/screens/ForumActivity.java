package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.example.sagivproject.utils.ForumHelper;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class ForumActivity extends AppCompatActivity {
    private Button btnToMain, btnToContact, btnToDetailsAboutUser, btnToExit, btnSendMessage;
    private ForumHelper forumHelper;
    private EditText edtNewMessage;
    private RecyclerView recyclerForum;

    private ForumAdapter adapter;
    private List<ForumMessage> messageList;

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

        /* ------------
            Forum Logic
           ------------ */
        View root = findViewById(R.id.forumPage);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    imeInsets.bottom
            );
            return insets;
        });

        recyclerForum.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        adapter = new ForumAdapter(messageList);
        recyclerForum.setAdapter(adapter);

        forumHelper = new ForumHelper(this, messageList, recyclerForum, adapter);

        adapter.setForumMessageListener(new ForumAdapter.ForumMessageListener() {
            @Override
            public void onClick(ForumMessage message) {
                forumHelper.deleteMessage(message);
            }

            @Override
            public boolean isShowMenuOptions(ForumMessage message) {
                User user = SharedPreferencesUtil.getUser(ForumActivity.this);
                boolean isOwner = message.getUserId() != null && message.getUserId().equals(user.getUid()); //משתמש רגיל - רק את שלו
                return isOwner;
            }
        });

        btnSendMessage.setOnClickListener(v -> forumHelper.sendMessage(edtNewMessage));
    }

    @Override
    protected void onResume() {
        super.onResume();
        forumHelper.loadMessages();
    }
}