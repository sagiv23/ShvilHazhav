package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;

import java.text.MessageFormat;

/**
 * A RecyclerView adapter for displaying a leaderboard of users based on their game wins.
 * <p>
 * This adapter takes a list of {@link User} objects, sorted by win count, and displays them.
 * It gives a special visual treatment (a trophy emoji) to the top-ranked player.
 * It uses {@link ListAdapter} with a {@link GenericDiffCallback} for efficient list updates.
 * </p>
 */
public class LeaderboardAdapter extends ListAdapter<User, LeaderboardAdapter.ViewHolder> {

    /**
     * Constructs a new LeaderboardAdapter.
     */
    public LeaderboardAdapter() {
        super(new GenericDiffCallback<>());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_rank_in_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = getItem(position);
        holder.tvName.setText(user.getFullName());

        // Add a trophy for the first place user
        if (position == 0 && user.getCountWins() > 0) {
            holder.tvWins.setText(MessageFormat.format("\uD83E\uDD47 {0}", user.getCountWins())); // 🥇
        } else {
            holder.tvWins.setText(String.valueOf(user.getCountWins()));
        }
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvWins;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ItemUserRank_user_name);
            tvWins = itemView.findViewById(R.id.tv_ItemUserRank_wins);
        }
    }
}
