package com.example.sagivproject.adapters;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.services.impl.AdapterService;
import com.example.sagivproject.ui.CustomTypefaceSpan;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

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
 * <li>Custom Hebrew typography using {@link CustomTypefaceSpan}.</li>
 * <li>Real-time visual feedback for the currently speaking message.</li>
 * </ul>
 * </p>
 */
public class ForumAdapter extends BaseAdapter<ForumMessage, ForumAdapter.ForumViewHolder> {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ForumMessageListener listener;
    private TextToSpeech tts;
    private String currentlySpeakingMsgId = null;

    /**
     * Constructs a new ForumAdapter.
     * Hilt provides instances of this adapter via {@link AdapterService}.
     */
    @Inject
    public ForumAdapter() {
    }

    /**
     * Initializes the Text-to-Speech engine and begins speaking the provided message.
     *
     * @param v   A view used to retrieve context for TTS initialization.
     * @param msg The {@link ForumMessage} to read aloud after initialization.
     */
    private void initTTS(View v, ForumMessage msg) {
        if (tts == null) {
            tts = new TextToSpeech(v.getContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("he", "IL"));
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            currentlySpeakingMsgId = utteranceId;
                            mainHandler.post(() -> notifyItemChangedById(utteranceId));
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if (utteranceId.equals(currentlySpeakingMsgId)) {
                                currentlySpeakingMsgId = null;
                            }
                            mainHandler.post(() -> notifyItemChangedById(utteranceId));
                        }

                        @Override
                        public void onError(String utteranceId) {
                            if (utteranceId.equals(currentlySpeakingMsgId)) {
                                currentlySpeakingMsgId = null;
                            }
                            mainHandler.post(() -> notifyItemChangedById(utteranceId));
                        }
                    });
                    speak(msg);
                }
            });
        }
    }

    /**
     * Finds a message by its ID and triggers a partial UI refresh for that item.
     *
     * @param msgId The unique ID of the message.
     */
    private void notifyItemChangedById(String msgId) {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getId().equals(msgId)) {
                notifyItemChanged(i);
                break;
            }
        }
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

        String senderName = msg.getSenderName() != null ? msg.getSenderName() : "אנונימי";
        Typeface customFont = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.text_hebrew);
        SpannableString userNameSpannable = new SpannableString(senderName);

        if (customFont != null) {
            userNameSpannable.setSpan(
                    new CustomTypefaceSpan("", customFont),
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

        boolean isThisSpeaking = msg.getId().equals(currentlySpeakingMsgId);
        holder.btnSpeak.setIconResource(isThisSpeaking ? R.drawable.ic_x : R.drawable.ic_sound);
        holder.btnSpeak.setContentDescription(isThisSpeaking ? "בטל השמעה" : "השמעה");

        holder.btnSpeak.setOnClickListener(v -> {
            if (msg.getId().equals(currentlySpeakingMsgId)) {
                tts.stop();
                currentlySpeakingMsgId = null;
                notifyItemChanged(position);
            } else {
                String oldId = currentlySpeakingMsgId;
                if (oldId != null) {
                    tts.stop();
                    currentlySpeakingMsgId = null;
                    notifyItemChangedById(oldId);
                }

                if (tts == null) {
                    initTTS(v, msg);
                } else {
                    speak(msg);
                }
            }
        });

        if (listener != null && listener.isShowMenuOptions(msg)) {
            holder.btnMenu.setVisibility(View.VISIBLE);
            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenu);
                popup.getMenuInflater().inflate(R.menu.menu_forum_message, popup.getMenu());

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

    /**
     * Triggers the TTS engine to speak the text of the given message.
     *
     * @param msg The message to speak.
     */
    private void speak(ForumMessage msg) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, msg.getId());
            tts.speak(msg.getMessage(), TextToSpeech.QUEUE_FLUSH, params, msg.getId());
        }
    }

    /**
     * Shuts down the TTS engine and cleans up resources.
     * Must be called from the activity's {@code onDestroy}.
     */
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
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
    }

    /**
     * ViewHolder for forum message items.
     */
    public static class ForumViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        /**
         * TextViews for sender, metadata, and content.
         */
        final TextView txtUser, txtEmail, txtIsAdmin, txtMessage, txtTime;

        /**
         * Button to open the moderation menu.
         */
        final ImageButton btnMenu;

        /**
         * Button to toggle TTS playback for the message.
         */
        final MaterialButton btnSpeak;

        /**
         * Constructs a new ForumViewHolder.
         *
         * @param itemView The view representing a single forum message.
         */
        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
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