package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<User> userList;

    public LeaderboardAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_rank_in_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvWins.setText(String.valueOf(user.getCountWins()));
        holder.tvRole.setText(user.getIsAdmin() ? "מנהל" : "משתמש");

        if (position == 0 && user.getCountWins() > 0) {
            holder.tvWins.setText("🥇 " + user.getCountWins());
        } else {
            holder.tvWins.setText(String.valueOf(user.getCountWins()));
        }
    }

    @Override
    public int getItemCount() { return userList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole, tvWins;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ItemUserRank_user_name);
            tvEmail = itemView.findViewById(R.id.tv_ItemUserRank_user_email);
            tvRole = itemView.findViewById(R.id.tv_ItemUserRank_role);
            tvWins = itemView.findViewById(R.id.tv_ItemUserRank_wins);

        }
    }
}