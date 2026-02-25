package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.GameRoom;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * A RecyclerView adapter for displaying a log of {@link GameRoom} objects.
 * <p>
 * This adapter is intended for administrative or debugging purposes, showing a real-time
 * list of all game rooms and their current state, including players, score, and status.
 * It uses {@link AsyncListDiffer} with a {@link GenericDiffCallback} for efficient list updates.
 * </p>
 */
public class MemoryGameLogAdapter extends RecyclerView.Adapter<MemoryGameLogAdapter.ViewHolder> {
    private Map<String, String> uidToNameMap;
    private final AsyncListDiffer<GameRoom> differ = new AsyncListDiffer<>(this, new GenericDiffCallback<>());

    /**
     * Constructs a new MemoryGameLogAdapter.
     *
     * @param uidToNameMap A map to resolve user UIDs to displayable names.
     */
    public MemoryGameLogAdapter(Map<String, String> uidToNameMap) {
        this.uidToNameMap = uidToNameMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRoom room = differ.getCurrentList().get(position);

        String p1Name = uidToNameMap.getOrDefault(room.getPlayer1Uid(), "שחקן לא ידוע");
        String p2Name = uidToNameMap.getOrDefault(room.getPlayer2Uid(), "ממתין...");

        holder.txtPlayers.setText(String.format("%s נגד %s", p1Name, p2Name));
        holder.txtScore.setText(MessageFormat.format("תוצאה: {0} - {1}", room.getPlayer1Score(), room.getPlayer2Score()));
        holder.txtStatus.setText(String.format("סטטוס: %s", room.getStatus()));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    /**
     * Submits a new list of rooms to the adapter.
     *
     * @param newRooms The new list of game rooms.
     */
    public void submitList(List<GameRoom> newRooms) {
        differ.submitList(newRooms);
    }

    /**
     * Submits a new list of rooms and an updated UID-to-name map to the adapter.
     *
     * @param newRooms The new list of game rooms.
     * @param newMap   The new map of UIDs to names.
     */
    public void submitData(List<GameRoom> newRooms, Map<String, String> newMap) {
        this.uidToNameMap = newMap;
        submitList(newRooms);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtPlayers, txtScore, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlayers = itemView.findViewById(R.id.txt_itemGameLog_players);
            txtScore = itemView.findViewById(R.id.txt_itemGameLog_score);
            txtStatus = itemView.findViewById(R.id.txt_itemGameLog_status);
        }
    }
}
