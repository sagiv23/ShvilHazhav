package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.GameRoom;

import java.util.List;

public class MemoryGameLogAdapter extends RecyclerView.Adapter<MemoryGameLogAdapter.ViewHolder> {
    private List<GameRoom> gameRooms;

    public MemoryGameLogAdapter(List<GameRoom> gameRooms) {
        this.gameRooms = gameRooms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRoom room = gameRooms.get(position);

        String p1Name = room.getPlayer1() != null ? room.getPlayer1().getFullName() : "שחקן לא ידוע";
        String p2Name = room.getPlayer2() != null ? room.getPlayer2().getFullName() : "ממתין...";

        holder.txtPlayers.setText(p1Name + " נגד " + p2Name);
        holder.txtScore.setText("תוצאה: " + room.getPlayer1Score() + " - " + room.getPlayer2Score());
        holder.txtStatus.setText("סטטוס: " + room.getStatus());
    }

    @Override
    public int getItemCount() {
        return gameRooms.size();
    }

    public void updateData(List<GameRoom> newRooms) {
        this.gameRooms = newRooms;
        notifyDataSetChanged(); //מעדכן את ה-RecyclerView על שינוי בנתונים
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPlayers, txtScore, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlayers = itemView.findViewById(R.id.txt_itemGameLog_players);
            txtScore = itemView.findViewById(R.id.txt_itemGameLog_score);
            txtStatus = itemView.findViewById(R.id.txt_itemGameLog_status);
        }
    }
}