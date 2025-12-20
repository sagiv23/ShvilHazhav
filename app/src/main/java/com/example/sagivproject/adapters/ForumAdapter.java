package com.example.sagivproject.adapters;

import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumMessage;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.MenuItem;

import androidx.core.content.res.ResourcesCompat;

import com.example.sagivproject.utils.CustomTypefaceSpan;

import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {
    private final List<ForumMessage> messages;
    private ForumMessageListener listener;

    public interface ForumMessageListener {
        void onClick(ForumMessage message);
        boolean isShowMenuOptions(ForumMessage message);
    }

    public void setForumMessageListener(ForumMessageListener listener) {
        this.listener = listener;
    }

    public ForumAdapter(List<ForumMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forum_message, parent, false);
        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumViewHolder holder, int position) {
        ForumMessage msg = messages.get(position);

        holder.txtUser.setText(msg.getFullName());
        holder.txtEmail.setText(msg.getEmail());
        holder.txtIsAdmin.setText(msg.getIsUserAdmin() ? "מנהל" : "משתמש רגיל");
        holder.txtMessage.setText(msg.getMessage());
        holder.txtTime.setText(DateFormat.format("dd/MM/yyyy HH:mm", msg.getTimestamp()));

        if (listener != null && listener.isShowMenuOptions(msg)) {
            holder.btnMenu.setVisibility(View.VISIBLE);

            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenu);
                popup.getMenuInflater().inflate(R.menu.forum_message_menu, popup.getMenu());

                Typeface typeface = ResourcesCompat.getFont(
                        v.getContext(),
                        R.font.text_hebrew
                );

                MenuItem deleteItem = popup.getMenu().findItem(R.id.action_delete);
                if (deleteItem != null) {
                    SpannableString title = new SpannableString(deleteItem.getTitle());
                    title.setSpan(
                            new CustomTypefaceSpan("", typeface),
                            0,
                            title.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    title.setSpan(
                            new AbsoluteSizeSpan(20, true),
                            0,
                            title.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    deleteItem.setTitle(title);
                }

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        listener.onClick(msg);
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

    public static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtEmail, txtIsAdmin, txtMessage, txtTime;
        ImageButton btnMenu;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.ItemForumMessageTxtUser);
            txtEmail = itemView.findViewById(R.id.ItemForumMessageTxtEmail);
            txtIsAdmin = itemView.findViewById(R.id.ItemForumMessageTxtIsAdmin);
            txtMessage = itemView.findViewById(R.id.ItemForumMessageTxtMessage);
            txtTime = itemView.findViewById(R.id.ItemForumMessageTxtTime);
            btnMenu = itemView.findViewById(R.id.ItemForumMessageBtnMenu);
        }
    }
}

