package com.example.sagivproject.adapters;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.ForumMessage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a list of {@link ForumMessage} objects.
 * <p>
 * This adapter handles the binding of forum message data to views and includes:
 * <ul>
 * <li>Dynamic user data display: Sender details (name, email, role) are pre-populated
 * by the service layer into the {@link ForumMessage} object.</li>
 * <li>Text-to-Speech (TTS) integration for reading messages aloud.</li>
 * <li>Admin moderation tools (conditional menu for deletion).</li>
 * <li>Real-time visual feedback for the currently speaking message.</li>
 * </ul>
 * </p>
 */
public class ForumAdapter extends BaseAdapter<ForumMessage, ForumAdapter.ForumViewHolder> {
    private ForumMessageListener listener;
    private Typeface cachedFont;
    private String currentUserId;

    /**
     * Constructs a new ForumAdapter.
     */
    @Inject
    public ForumAdapter() {
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    /**
     * Sets the listener for message-specific actions (e.g., deletion).
     *
     * @param listener The {@link ForumMessageListener} implementation.
     */
    public void setForumMessageListener(ForumMessageListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the entire message list.
     *
     * @param newMessages The new list of {@link ForumMessage} objects.
     */
    public void setMessages(List<ForumMessage> newMessages) {
        setData(newMessages);
    }

    /**
     * Adds older messages to the beginning of the list for pagination.
     *
     * @param olderMessages List of older {@link ForumMessage} objects to prepend.
     */
    public void addOlderMessages(List<ForumMessage> olderMessages) {
        if (olderMessages == null || olderMessages.isEmpty()) return;
        dataList.addAll(0, olderMessages);
        notifyItemRangeInserted(0, olderMessages.size());
    }

    /**
     * Removes a specific message from the list and notifies the adapter.
     *
     * @param message The message object to remove.
     */
    public void removeMessage(ForumMessage message) {
        int index = dataList.indexOf(message);
        if (index != -1) {
            dataList.remove(index);
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
        ForumMessage msg = getItem(position);

        // Alignment logic
        boolean isMine = msg.getUserId() != null && msg.getUserId().equals(currentUserId);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.cardView.getLayoutParams();
        params.gravity = isMine ? Gravity.START : Gravity.END;
        holder.cardView.setLayoutParams(params);

        if (cachedFont == null) {
            cachedFont = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.text_hebrew);
        }

        String senderName = msg.getSenderName() != null ? msg.getSenderName() : "אנונימי";
        SpannableString userNameSpannable = new SpannableString(senderName);

        if (cachedFont != null) {
            userNameSpannable.setSpan(
                    new TypefaceSpan(cachedFont),
                    0,
                    userNameSpannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        holder.txtUser.setText(userNameSpannable);
        holder.txtEmail.setText(msg.getSenderEmail());
        holder.txtIsAdmin.setText(msg.isSenderAdmin() ? "מנהל" : "משתמש");

        holder.txtMessage.setText(msg.getMessage());
        holder.txtTime.setText(DateFormat.format("dd/MM/yyyy HH:mm", msg.getTimestamp()));

        String currentlySpeakingMsgId = listener != null ? listener.getCurrentlySpeakingMsgId() : null;
        boolean isThisSpeaking = msg.getId().equals(currentlySpeakingMsgId);
        holder.btnSpeak.setIconResource(isThisSpeaking ? R.drawable.ic_x : R.drawable.ic_sound);
        holder.btnSpeak.setContentDescription(isThisSpeaking ? "בטל השמעה" : "השמעה");

        holder.btnSpeak.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSpeakClicked(msg);
            }
        });

        if (listener != null && listener.isShowMenuOptions(msg)) {
            holder.btnMenu.setVisibility(View.VISIBLE);
            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenu);
                popup.getMenuInflater().inflate(R.menu.menu_forum_message, popup.getMenu());

                MenuItem deleteItem = popup.getMenu().findItem(R.id.action_delete);
                if (deleteItem != null && cachedFont != null) {
                    SpannableString title = new SpannableString(deleteItem.getTitle());
                    title.setSpan(new TypefaceSpan(cachedFont), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    /**
     * Interface for handling interactions with forum messages.
     */
    public interface ForumMessageListener {
        /**
         * Called when a message is clicked (e.g., for deletion).
         *
         * @param message The clicked {@link ForumMessage}.
         */
        void onClick(ForumMessage message);

        /**
         * Determines whether the action menu should be displayed for a specific message.
         *
         * @param message The message to check.
         * @return true to show the menu, false otherwise.
         */
        boolean isShowMenuOptions(ForumMessage message);

        /**
         * Called when the speak button is clicked.
         *
         * @param message The message to speak or stop speaking.
         */
        void onSpeakClicked(ForumMessage message);

        /**
         * Returns the ID of the message currently being spoken.
         *
         * @return The message ID, or null if nothing is speaking.
         */
        String getCurrentlySpeakingMsgId();
    }

    /**
     * ViewHolder for forum message items.
     */
    public static class ForumViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView txtUser, txtEmail, txtIsAdmin, txtMessage, txtTime;
        final ImageButton btnMenu;
        final MaterialButton btnSpeak;
        final MaterialCardView cardView;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.ItemForumMessageCard);
            txtUser = itemView.findViewById(R.id.ItemForumMessageTxtUser);
            txtEmail = itemView.findViewById(R.id.ItemForumMessageTxtEmail);
            txtIsAdmin = itemView.findViewById(R.id.ItemForumMessageTxtIsAdmin);
            txtMessage = itemView.findViewById(R.id.ItemForumMessageTxtMessage);
            txtTime = itemView.findViewById(R.id.ItemForumMessageTxtTime);
            btnMenu = itemView.findViewById(R.id.ItemForumMessageBtnMenu);
            btnSpeak = itemView.findViewById(R.id.ItemForumMessageBtnSpeak);
        }
    }
}