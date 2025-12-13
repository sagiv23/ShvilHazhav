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
import com.example.sagivproject.utils.ForumHelper;
import com.example.sagivproject.utils.PagePermissions;

import java.util.ArrayList;
import java.util.List;

public class AdminForumActivity extends AppCompatActivity {
    private Button btnToAdminPage, btnSendMessage;
    private ForumHelper forumHelper;
    private EditText edtNewMessage;
    private RecyclerView recyclerForum;

    private ForumAdapter adapter;
    private List<ForumMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_forum);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminForumPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkAdminPage(this);

        btnToAdminPage = findViewById(R.id.btn_AdminForum_to_admin);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(AdminForumActivity.this, AdminPageActivity.class)));

        btnSendMessage = findViewById(R.id.btn_AdminForum_send_message);
        edtNewMessage = findViewById(R.id.edt_AdminForum_new_message);
        recyclerForum = findViewById(R.id.recycler_AdminForum);

        /* ------------
            Forum Logic
           ------------ */
        View root = findViewById(R.id.adminForumPage);

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
            public boolean isShowMenuOptions(ForumMessage message) { return true; } //מנהל בוודאות
        });

        btnSendMessage.setOnClickListener(v -> forumHelper.sendMessage(edtNewMessage));
    }

    @Override
    protected void onResume() {
        super.onResume();
        forumHelper.loadMessages();
    }
}