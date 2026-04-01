package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.services.impl.AdapterService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a real-time log of memory game sessions.
 * <p>
 * This adapter is used by administrators to monitor game history and status. It uses an
 * external UID-to-Name mapping to display human-readable participant names instead of
 * raw database IDs.
 * </p>
 */
public class MemoryGameLogAdapter extends BaseAdapter<GameRoom, MemoryGameLogAdapter.ViewHolder> {
    /** A map for resolving user UIDs to full names for display purposes. */
    private Map<String, String> uidToNameMap;

    /**
     * Constructs a new MemoryGameLogAdapter.
     * Hilt provides instances via {@link AdapterService}.
     */
    @Inject
    public MemoryGameLogAdapter() {
    }

    /**
     * Updates the game room data list.
     * @param newRooms The new list of {@link GameRoom} objects.
     */
    public void setRooms(List<GameRoom> newRooms) { setData(newRooms); }

    /**
     * Updates both the data list and the name resolution map simultaneously.
     * @param newRooms The new list of {@link GameRoom} objects.
     * @param newMap The updated UID-to-Name resolution map.
     */
    public void submitData(List<GameRoom> newRooms, Map<String, String> newMap) {
        this.uidToNameMap = newMap;
        setRooms(newRooms);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRoom room = getItem(position);

        String p1Name = uidToNameMap != null ? uidToNameMap.getOrDefault(room.getPlayer1Uid(), "אנונימי") : "אנונימי";
        String p2Name = uidToNameMap != null ? uidToNameMap.getOrDefault(room.getPlayer2Uid(), "ממתין...") : "ממתין...";

        holder.txtPlayers.setText(String.format("%s נגד %s", p1Name, p2Name));
        holder.txtScore.setText(MessageFormat.format("תוצאה: {0} - {1}", room.getPlayer1Score(), room.getPlayer2Score()));
        holder.txtStatus.setText(String.format("סטטוס: %s", room.getStatus()));
    }

    /** ViewHolder class for memory game log entries. */
    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        /** TextViews for player names, scores, and room status. */
        final TextView txtPlayers, txtScore, txtStatus;

        /**
         * Constructs a new ViewHolder.
         * @param itemView The view representing a single game log row.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlayers = itemView.findViewById(R.id.txt_itemGameLog_players);
            txtScore = itemView.findViewById(R.id.txt_itemGameLog_score);
            txtStatus = itemView.findViewById(R.id.txt_itemGameLog_status);
        }
    }
}