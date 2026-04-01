package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.User;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a leaderboard of users based on their memory game performance.
 * <p>
 * This adapter manages the display of usernames and their total wins across all dates.
 * It highlights the top-ranked user with a special "Gold Medal" icon (emoji) for better engagement.
 * </p>
 */
public class LeaderboardAdapter extends BaseAdapter<User, LeaderboardAdapter.ViewHolder> {
    /**
     * Constructs a new LeaderboardAdapter.
     * Use {@link #setUsers(List)} to populate the list after creation.
     */
    @Inject
    public LeaderboardAdapter() {
    }

    /**
     * Sets the list of users to be displayed in the leaderboard.
     * @param users The list of {@link User} objects, typically pre-sorted by total wins.
     */
    public void setUsers(List<User> users) { setData(users); }

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

        int totalWins = getTotalWins(user);

        if (position == 0 && totalWins > 0) {

            holder.tvWins.setText(MessageFormat.format("\uD83E\uDD47 {0}", totalWins));
        } else {
            holder.tvWins.setText(String.valueOf(totalWins));
        }
    }

    /**
     * Calculates the sum of all memory game wins across the user's daily statistics.
     * @param u The user to calculate wins for.
     * @return The total number of wins.
     */
    private int getTotalWins(User u) {
        int total = 0;
        if (u.getDailyStats() != null) {
            for (DailyStats ds : u.getDailyStats().values()) {
                total += ds.getMemoryWins();
            }
        }
        return total;
    }

    /** ViewHolder class for leaderboard rows. */
    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        /** TextView for the user's full name. */
        final TextView tvName;

        /** TextView for the win count or rank. */
        final TextView tvWins;

        /**
         * Constructs a new ViewHolder.
         * @param itemView The view representing a single leaderboard row.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ItemUserRank_user_name);
            tvWins = itemView.findViewById(R.id.tv_ItemUserRank_wins);
        }
    }
}