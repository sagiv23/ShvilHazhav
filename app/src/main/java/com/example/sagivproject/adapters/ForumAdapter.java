package com.example.sagivproject.adapters;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;

import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {
    private final List<ForumMessage> messages;
    private final User user;

    public ForumAdapter(List<ForumMessage> messages, User user) {
        this.messages = messages;
        this.user = user;
    }

    @NonNull
    @Override
    public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forum_message, parent, false);
        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ForumMessage msg = messages.get(position);
        holder.txtUser.setText(msg.getFullName());
        holder.txtEmail.setText(msg.getEmail());
        holder.txtMessage.setText(msg.getMessage());
        holder.txtTime.setText(DateFormat.format("dd/MM/yyyy HH:mm", msg.getTimestamp()));

        // תפריט שלוש נקודות
        if (user != null && msg.getUserId() != null && msg.getUserId().equals(user.getUid())) {

            holder.btnMenu.setVisibility(View.VISIBLE);
            holder.btnMenu.setOnClickListener(v -> {
                ContextThemeWrapper wrapper = new ContextThemeWrapper(v.getContext(), R.style.Theme_SagivProject);
                PopupMenu popup = new PopupMenu(wrapper, holder.btnMenu);
                popup.getMenuInflater().inflate(R.menu.forum_message_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        DatabaseService.getInstance().deleteForumMessage(msg.getMessageId(), new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Toast.makeText(v.getContext(), "ההודעה נמחקה", Toast.LENGTH_SHORT).show();
                                messages.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, messages.size());
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(v.getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                            }
                        });

                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        } else {
            holder.btnMenu.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtEmail, txtMessage, txtTime;
        ImageButton btnMenu;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.ItemForumMessageTxtUser);
            txtEmail = itemView.findViewById(R.id.ItemForumMessageTxtEmail);
            txtMessage = itemView.findViewById(R.id.ItemForumMessageTxtMessage);
            txtTime = itemView.findViewById(R.id.ItemForumMessageTxtTime);
            btnMenu = itemView.findViewById(R.id.ItemForumMessageBtnMenu);
        }
    }
}
