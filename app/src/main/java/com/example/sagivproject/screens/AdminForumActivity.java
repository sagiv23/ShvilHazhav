package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminForumActivity extends AppCompatActivity {
    private Button btnToAdminPage, btnSendMessage;
    private ForumHelper forumHelper;
    private EditText edtNewMessage;
    private RecyclerView recyclerForum;

    private ForumAdapter adapter;
    private List<ForumMessage> messageList;
    private LinearLayoutManager layoutManager;

    private boolean userAtBottom = true;

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

        messageList = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);

        recyclerForum.setLayoutManager(layoutManager);

        // יוצרים ForumHelper לוקאלי אחרי Adapter
        adapter = new ForumAdapter(messageList, SharedPreferencesUtil.getUser(this), null);
        recyclerForum.setAdapter(adapter);

        forumHelper = new ForumHelper(this, messageList, recyclerForum, adapter, layoutManager);

        // עכשיו מחברים את ForumHelper ל־Adapter
        adapter.setForumHelper(forumHelper); // <-- צריך להוסיף setter ב־Adapter

        forumHelper.loadMessages();
        btnSendMessage.setOnClickListener(v -> forumHelper.sendMessage(edtNewMessage));

        // Scroll listener
        recyclerForum.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                userAtBottom = (lastVisible == adapter.getItemCount() - 1);
            }
        });
    }
}