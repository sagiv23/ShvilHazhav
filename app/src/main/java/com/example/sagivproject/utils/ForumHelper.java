package com.example.sagivproject.utils;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;

import java.util.List;

public class ForumHelper {
    private final Context context;
    private final ForumAdapter adapter;
    private final List<ForumMessage> messageList;
    private final RecyclerView recycler;;

    private boolean userAtBottom = true;

    public ForumHelper(Context context,
                       List<ForumMessage> messageList,
                       RecyclerView recycler,
                       ForumAdapter adapter) {

        this.context = context;
        this.messageList = messageList;
        this.recycler = recycler;
        this.adapter = adapter;
    }

    public void loadMessages() {
        DatabaseService.getInstance().getForumMessagesRealtime(new DatabaseService.DatabaseCallback<List<ForumMessage>>() {
            @Override
            public void onCompleted(List<ForumMessage> list) {
                int oldSize = messageList.size();

                messageList.clear();
                messageList.addAll(list);
                adapter.notifyDataSetChanged();

                //אם נוספה הודעה חדשה והמשתמש היה בתחתית - נגלול אליה
                if (userAtBottom && messageList.size() > oldSize) {
                    recycler.post(() ->
                            recycler.smoothScrollToPosition(messageList.size() - 1)
                    );
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(context, "שגיאה בטעינת הודעות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendMessage(EditText edt) {
        String text = edt.getText().toString().trim();
        if (text.isEmpty()) return;

        User savedUser = SharedPreferencesUtil.getUser(context);

        String messageId = DatabaseService.getInstance().generateForumMessageId();
        ForumMessage msg = new ForumMessage(
                messageId,
                savedUser.getFullName(),
                savedUser.getEmail(),
                text,
                System.currentTimeMillis(),
                savedUser.getUid(),
                savedUser.getIsAdmin()
        );

        DatabaseService.getInstance().sendForumMessage(msg, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void obj) {
                edt.setText("");
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(context, "שגיאה בשליחת ההודעה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteMessage(ForumMessage msg) {
        DatabaseService.getInstance().deleteForumMessage(msg.getMessageId(),
                new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void obj) {
                        Toast.makeText(context, "ההודעה נמחקה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(context, "שגיאה במחיקת הודעה", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
