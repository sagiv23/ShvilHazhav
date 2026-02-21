package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.ImageUtil;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

/**
 * A RecyclerView adapter for displaying a table of {@link User} objects for administrative purposes.
 * <p>
 * This adapter binds detailed user information to a row layout. It provides controls for
 * administrators to perform actions such as deleting a user or toggling their admin status.
 * It also handles click events for editing a user or viewing their profile picture.
 * </p>
 */
public class UsersTableAdapter extends RecyclerView.Adapter<UsersTableAdapter.UserViewHolder> {
    private final User currentUser;
    private final List<User> users;
    private final OnUserActionListener listener;

    /**
     * Constructs a new UsersTableAdapter.
     *
     * @param users       The list of users to display.
     * @param currentUser The currently logged-in admin user, used to prevent self-modification.
     * @param listener    The listener for user action events.
     */
    public UsersTableAdapter(List<User> users, User currentUser, OnUserActionListener listener) {
        this.users = users;
        this.currentUser = currentUser;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Bind user data to views
        holder.txtUserFullName.setText(user.getFullName());
        holder.txtUserEmail.setText(user.getEmail());
        holder.txtUserPassword.setText(String.format("סיסמה: %s", user.getPassword()));
        holder.txtUserAge.setText(MessageFormat.format("גיל: {0}", user.getAge()));
        holder.txtUserIsAdmin.setText(String.format("מנהל: %s", user.isAdmin() ? "כן" : "לא"));
        holder.txtUserWins.setText(MessageFormat.format("ניצחונות: {0}", user.getCountWins()));

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(user.getBirthDateMillis());
        String birthDateStr = String.format(Locale.ROOT, "%02d/%02d/%04d",
                cal.get(java.util.Calendar.DAY_OF_MONTH), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR));
        holder.txtUserBirthDate.setText(String.format("תאריך לידה: %s", birthDateStr));

        ImageUtil.loadImage(user.getProfileImage(), holder.imgUserProfile);

        // Configure admin actions
        boolean isSelf = user.equals(currentUser);
        if (isSelf) {
            // Admin cannot toggle their own admin status or delete themselves from this screen
            holder.btnToggleAdmin.setVisibility(View.GONE);
        } else {
            holder.btnToggleAdmin.setVisibility(View.VISIBLE);
            holder.btnDeleteUser.setVisibility(View.VISIBLE);

            // Set icon based on current admin status
            if (user.isAdmin()) {
                holder.btnToggleAdmin.setImageResource(R.drawable.ic_remove_admin);
                holder.btnToggleAdmin.setContentDescription("הסר מנהל");
            } else {
                holder.btnToggleAdmin.setImageResource(R.drawable.ic_add_admin);
                holder.btnToggleAdmin.setContentDescription("הפוך למנהל");
            }

            holder.btnToggleAdmin.setOnClickListener(v -> listener.onToggleAdmin(user));
        }

        holder.btnDeleteUser.setOnClickListener(v -> listener.onDeleteUser(user));

        // Set long-click listener to edit user details
        holder.itemView.setOnLongClickListener(v -> {
            listener.onUserClicked(user);
            return true;
        });

        holder.imgUserProfile.setOnClickListener(v -> listener.onUserImageClicked(user, holder.imgUserProfile));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * An interface for handling administrative actions on a user item.
     */
    public interface OnUserActionListener {
        /**
         * Called when the toggle admin button is clicked for a user.
         *
         * @param user The user whose admin status should be toggled.
         */
        void onToggleAdmin(User user);

        /**
         * Called when the delete button is clicked for a user.
         *
         * @param user The user to be deleted.
         */
        void onDeleteUser(User user);

        /**
         * Called when a user item is long-clicked, intended for editing.
         *
         * @param user The user that was clicked.
         */
        void onUserClicked(User user);

        /**
         * Called when a user's profile image is clicked.
         *
         * @param user      The user whose image was clicked.
         * @param imageView The ImageView that was clicked.
         */
        void onUserImageClicked(User user, ImageView imageView);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView txtUserFullName, txtUserEmail, txtUserPassword, txtUserIsAdmin, txtUserWins, txtUserAge, txtUserBirthDate;
        final ImageButton btnDeleteUser, btnToggleAdmin;
        final ImageView imgUserProfile;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserFullName = itemView.findViewById(R.id.txt_UserRow_fullName);
            txtUserEmail = itemView.findViewById(R.id.txt_UserRow_email);
            txtUserAge = itemView.findViewById(R.id.txt_UserRow_age);
            txtUserBirthDate = itemView.findViewById(R.id.txt_UserRow_birthDate);
            txtUserPassword = itemView.findViewById(R.id.txt_UserRow_password);
            txtUserIsAdmin = itemView.findViewById(R.id.txt_UserRow_IsAdmin);
            imgUserProfile = itemView.findViewById(R.id.img_UserRow_userProfile);
            txtUserWins = itemView.findViewById(R.id.txt_UserRow_wins);
            btnDeleteUser = itemView.findViewById(R.id.btn_UserRow_DeleteUser);
            btnToggleAdmin = itemView.findViewById(R.id.btn_UserRow_MakeAdmin);
        }
    }
}
