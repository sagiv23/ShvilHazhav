package com.example.sagivproject.adapters;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
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
import com.example.sagivproject.ui.CustomTypefaceSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView adapter for displaying a list of {@link ForumMessage} objects.
 * <p>
 * This adapter handles the binding of forum message data to the corresponding views in the
 * item layout. It also manages a popup menu for message actions, such as deletion,
 * based on permissions determined by a {@link ForumMessageListener}.
 * </p>
 */
public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {
    private final List<ForumMessage> messageList;
    private ForumMessageListener listener;

    public ForumAdapter() {
        this.messageList = new ArrayList<>();
    }

    /**
     * Sets the listener for message actions.
     *
     * @param listener The listener to be notified of user actions.
     */
    public void setForumMessageListener(ForumMessageListener listener) {
        this.listener = listener;
    }

    public void setMessages(List<ForumMessage> newMessages) {
        messageList.clear();
        messageList.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void removeMessage(ForumMessage message) {
        int index = messageList.indexOf(message);
        if (index != -1) {
            messageList.remove(index);
            notifyItemRemoved(index);
        }
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
        ForumMessage msg = messageList.get(position);

        Typeface customFont = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.text_hebrew);
        SpannableString userNameSpannable = new SpannableString(msg.getFullName());

        if (customFont != null) {
            userNameSpannable.setSpan(
                    new CustomTypefaceSpan("", customFont),
                    0,
                    userNameSpannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        holder.txtUser.setText(userNameSpannable);

        holder.txtEmail.setText(msg.getEmail());
        holder.txtIsAdmin.setText(msg.isSentByAdmin() ? "מנהל" : "משתמש רגיל");
        holder.txtMessage.setText(msg.getMessage());
        holder.txtTime.setText(DateFormat.format("dd/MM/yyyy HH:mm", msg.getTimestamp()));

        // Show or hide the menu button based on permissions
        if (listener != null && listener.isShowMenuOptions(msg)) {
            holder.btnMenu.setVisibility(View.VISIBLE);
            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenu);
                popup.getMenuInflater().inflate(R.menu.menu_forum_message, popup.getMenu());

                // Apply custom font and size to the menu item
                MenuItem deleteItem = popup.getMenu().findItem(R.id.action_delete);
                if (deleteItem != null && customFont != null) {
                    SpannableString title = new SpannableString(deleteItem.getTitle());
                    title.setSpan(new CustomTypefaceSpan("", customFont), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    title.setSpan(new AbsoluteSizeSpan(20, true), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        return messageList.size();
    }

    /**
     * An interface for handling actions on a forum message item.
     */
    public interface ForumMessageListener {
        /**
         * Called when a message action (e.g., delete) is clicked.
         *
         * @param message The message that was acted upon.
         */
        void onClick(ForumMessage message);

        /**
         * Determines whether the action menu should be shown for a given message.
         *
         * @param message The message to check.
         * @return True to show the menu, false otherwise.
         */
        boolean isShowMenuOptions(ForumMessage message);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class ForumViewHolder extends RecyclerView.ViewHolder {
        final TextView txtUser, txtEmail, txtIsAdmin, txtMessage, txtTime;
        final ImageButton btnMenu;

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
