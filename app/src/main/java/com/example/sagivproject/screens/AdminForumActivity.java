package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseForumActivity;
import com.example.sagivproject.models.ForumMessage;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The AdminForumActivity class displays the forum screen for an administrator.
 * It allows the admin to manage forum messages, including sending and deleting them.
 * This activity extends {@link BaseForumActivity} and implements {@link ForumPermissions}
 * to provide admin-specific functionalities.
 */
@AndroidEntryPoint
public class AdminForumActivity extends BaseForumActivity implements BaseForumActivity.ForumPermissions {

    /**
     * Called when the activity is first created.
     * This method initializes the activity's UI, sets up the top menu,
     * and configures the forum views and functionality.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        String categoryId = getIntent().getStringExtra("categoryId");
        String categoryName = getIntent().getStringExtra("categoryName");

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        Button btnSendMessage = findViewById(R.id.btn_AdminForum_send_message);
        EditText edtNewMessage = findViewById(R.id.edt_AdminForum_new_message);
        Button btnNewMessages = findViewById(R.id.btn_AdminForum_new_messages_indicator);
        RecyclerView recyclerForum = findViewById(R.id.recycler_AdminForum);
        btnSendMessage.setOnClickListener(v -> sendMessage());

        initForumViews(recyclerForum, edtNewMessage, btnNewMessages);
        this.permissions = this;
        setupForum(categoryId, categoryName);
    }

    /**
     * Determines if a forum message can be deleted.
     * For an admin, this always returns true.
     *
     * @param message The forum message to be checked.
     * @return Always true for an admin.
     */
    @Override
    public boolean canDelete(ForumMessage message) {
        return true;
    }

    /**
     * Called when the activity will start interacting with the user.
     * This method reloads the forum messages to ensure the list is up-to-date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
    }
}
