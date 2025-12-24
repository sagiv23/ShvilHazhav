package com.example.sagivproject.screens.forum;

import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.base.BaseActivity;
import com.example.sagivproject.services.ForumService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseForumActivity extends BaseActivity {
    protected RecyclerView recycler;
    protected EditText edtMessage;
    protected ForumAdapter adapter;
    protected List<ForumMessage> messages = new ArrayList<>();

    protected ForumService forumService = new ForumService();
    protected ForumPermissions permissions;

    protected void initForumViews(RecyclerView recycler, EditText edtMessage) {
        this.recycler = recycler;
        this.edtMessage = edtMessage;
    }

    protected void setupForum() {
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ForumAdapter(messages);
        recycler.setAdapter(adapter);

        adapter.setForumMessageListener(new ForumAdapter.ForumMessageListener() {
            @Override
            public void onClick(ForumMessage message) {
                forumService.deleteMessage(message.getMessageId(), new ForumService.ForumCallback<>() {
                    @Override
                    public void onSuccess(Void data) {
                        Toast.makeText(BaseForumActivity.this,
                                "ההודעה נמחקה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(BaseForumActivity.this,
                                "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public boolean isShowMenuOptions(ForumMessage message) {
                return permissions.canDelete(message);
            }
        });
    }

    protected void loadMessages() {
        forumService.listenToMessages(
                messages,
                recycler,
                adapter,
                new ForumService.ForumCallback<>() {
                    @Override
                    public void onSuccess(List<ForumMessage> list) {}

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(BaseForumActivity.this,
                                "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    protected void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        User user = SharedPreferencesUtil.getUser(this);

        forumService.sendMessage(user, text, new ForumService.ForumCallback<>() {
            @Override
            public void onSuccess(Void data) {
                edtMessage.setText("");
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(BaseForumActivity.this,
                        "שגיאה בשליחה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
