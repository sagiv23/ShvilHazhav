package com.example.sagivproject.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.CustomTypefaceSpan;
import com.example.sagivproject.utils.ForumHelper;

import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {
    private ForumHelper forumHelper;
    private final List<ForumMessage> messages;
//    private final User user;


    public interface Bla {
        public void onClick(ForumMessage message);


        public boolean isShowMenuOptions(ForumMessage message);
    }

    Bla bla;

    public ForumAdapter(List<ForumMessage> messages, User user, ForumHelper forumHelper) {
        this.messages = messages;
//        this.user = user;
        this.forumHelper = forumHelper;
    }

    public void setForumHelper(ForumHelper helper) {
        this.forumHelper = helper;
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

//        boolean isOwner = (user != null && msg.getUserId() != null && msg.getUserId().equals(user.getUid()));
//        boolean isAdmin = (user != null && user.getIsAdmin());

        // אם המנהל → להראות תפריט מחיקה לכל הודעה
        // אם המשתמש רגיל → רק להודעות שהוא כתב
//        if (isAdmin || isOwner) {
          if (bla.isShowMenuOptions(msg)) {
            holder.btnMenu.setVisibility(View.VISIBLE);

            holder.btnMenu.setOnClickListener(v -> {
                bla.onClick(msg);
//                ContextThemeWrapper wrapper = new ContextThemeWrapper(v.getContext(), R.style.Theme_SagivProject);
//                PopupMenu popup = new PopupMenu(wrapper, holder.btnMenu);
//                popup.getMenuInflater().inflate(R.menu.forum_message_menu, popup.getMenu());
//
//                // עיצוב פונט של כפתור מחיקה
//                MenuItem deleteItem = popup.getMenu().findItem(R.id.action_delete);
//                if (deleteItem != null) {
//                    Typeface typeface = ResourcesCompat.getFont(v.getContext(), R.font.text);
//                    SpannableString s = new SpannableString(deleteItem.getTitle());
//                    s.setSpan(new CustomTypefaceSpan("", typeface), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    deleteItem.setTitle(s);
//                }
//
//                popup.setOnMenuItemClickListener(item -> {
//                    if (item.getItemId() == R.id.action_delete) {
//
//                        // מחיקה דרך ForumHelper!
//                        forumHelper.deleteMessage(msg);
//
//                        return true;
//                    }
//                    return false;
//                });
//
//                popup.show();
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
