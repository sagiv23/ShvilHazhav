package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.User;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a leaderboard of users based on their game wins.
 * <p>
 * This adapter manages the display of usernames and their respective win counts.
 * It highlights the top-ranked user with a special icon.
 * </p>
 */
public class LeaderboardAdapter extends BaseAdapter<User, LeaderboardAdapter.ViewHolder> {
    /**
     * Constructs a new LeaderboardAdapter.
     * Hilt uses this constructor to provide an instance.
     */
    @Inject
    public LeaderboardAdapter() {
    }

    /**
     * Sets the list of users to be displayed in the leaderboard.
     *
     * @param users The list of {@link User} objects, typically sorted by wins.
     */
    public void setUsers(List<User> users) {
        setData(users);
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

        // Special handling for the first place (gold medal icon)
        if (position == 0 && user.getCountWins() > 0) {
            holder.tvWins.setText(MessageFormat.format("\uD83E\uDD47 {0}", user.getCountWins())); // 🥇
        } else {
            holder.tvWins.setText(String.valueOf(user.getCountWins()));
        }
    }

    /**
     * ViewHolder class for leaderboard items.
     */
    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView tvName, tvWins;

        /**
         * Initializes the ViewHolder with the item view.
         *
         * @param itemView The view representing a single leaderboard row.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ItemUserRank_user_name);
            tvWins = itemView.findViewById(R.id.tv_ItemUserRank_wins);
        }
    }
}
