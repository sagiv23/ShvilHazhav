package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.GameRoom;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a log of {@link GameRoom} objects.
 * <p>
 * This adapter is used to show a history of memory games played, including the names
 * of the participants, the final scores, and the status of the game (e.g., COMPLETED).
 * It uses an external map to resolve user UIDs to human-readable names.
 * </p>
 */
public class MemoryGameLogAdapter extends BaseAdapter<GameRoom, MemoryGameLogAdapter.ViewHolder> {
    private Map<String, String> uidToNameMap;

    /**
     * Constructs a new MemoryGameLogAdapter.
     * Hilt uses this constructor to provide an instance.
     */
    @Inject
    public MemoryGameLogAdapter() {
    }

    /**
     * Updates the data set with a new list of game rooms.
     *
     * @param newRooms The new list of {@link GameRoom} objects.
     */
    public void setRooms(List<GameRoom> newRooms) {
        setData(newRooms);
    }

    /**
     * Submits both the game room data and the UID-to-name mapping.
     *
     * @param newRooms The new list of {@link GameRoom} objects.
     * @param newMap   A map where keys are user UIDs and values are full names.
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

        // Resolve player names using the provided map, or use defaults if not found
        String p1Name = uidToNameMap != null ? uidToNameMap.getOrDefault(room.getPlayer1Uid(), "שחקן לא ידוע") : "שחקן לא ידוע";
        String p2Name = uidToNameMap != null ? uidToNameMap.getOrDefault(room.getPlayer2Uid(), "ממתין...") : "ממתין...";

        holder.txtPlayers.setText(String.format("%s נגד %s", p1Name, p2Name));
        holder.txtScore.setText(MessageFormat.format("תוצאה: {0} - {1}", room.getPlayer1Score(), room.getPlayer2Score()));
        holder.txtStatus.setText(String.format("סטטוס: %s", room.getStatus()));
    }

    /**
     * ViewHolder class for game log items.
     */
    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView txtPlayers, txtScore, txtStatus;

        /**
         * Initializes the ViewHolder with the item view.
         *
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
