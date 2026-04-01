package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.ImageUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a comprehensive table of users for administrative management.
 * <p>
 * This adapter displays detailed user information including full name, email, birthdate,
 * and administrative status. It provides interactive buttons for:
 * <ul>
 * <li>Toggling administrative privileges (promoting/demoting users).</li>
 * <li>Deleting user accounts.</li>
 * <li>Viewing profile images in detail.</li>
 * </ul>
 * It prevents the current administrator from performing self-destructive actions (deletion/role change).
 * </p>
 */
public class UsersTableAdapter extends BaseAdapter<User, UsersTableAdapter.UserViewHolder> {
    private final ImageUtil imageUtil;
    private User currentUser;
    private OnUserActionListener listener;

    /**
     * Constructs a new UsersTableAdapter.
     * @param imageUtil A utility class for loading and processing profile images.
     */
    @Inject
    public UsersTableAdapter(ImageUtil imageUtil) { this.imageUtil = imageUtil; }

    /**
     * Initializes the adapter with state-specific data and a listener.
     * @param currentUser The currently logged-in administrator (used for self-protection logic).
     * @param listener The {@link OnUserActionListener} to handle administrative events.
     */
    public void init(User currentUser, OnUserActionListener listener) {
        this.currentUser = currentUser;
        this.listener = listener;
    }

    /**
     * Updates the user list to be displayed in the table.
     * @param users The list of {@link User} objects.
     */
    public void setUserList(List<User> users) { setData(users); }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getItem(position);

        holder.txtUserFullName.setText(user.getFullName());
        holder.txtUserEmail.setText(user.getEmail());
        holder.txtUserIsAdmin.setText(String.format("מנהל: %s", user.isAdmin() ? "כן" : "לא"));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(user.getBirthDateMillis());
        String birthDateStr = String.format(Locale.ROOT, "%02d/%02d/%04d",
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        holder.txtUserBirthDate.setText(String.format("תאריך לידה: %s", birthDateStr));

        imageUtil.loadImage(user.getProfileImage(), holder.imgUserProfile);

        boolean isSelf = user.equals(currentUser);
        holder.btnToggleAdmin.setVisibility(isSelf ? View.GONE : View.VISIBLE);
        holder.btnDeleteUser.setVisibility(isSelf ? View.GONE : View.VISIBLE);

        if (!isSelf && listener != null) {
            if (user.isAdmin()) {
                holder.btnToggleAdmin.setImageResource(R.drawable.ic_remove_admin);
                holder.btnToggleAdmin.setContentDescription("הסר מנהל");
            } else {
                holder.btnToggleAdmin.setImageResource(R.drawable.ic_add_admin);
                holder.btnToggleAdmin.setContentDescription("הפוך למנהל");
            }

            holder.btnToggleAdmin.setOnClickListener(v -> listener.onToggleAdmin(user));
            holder.btnDeleteUser.setOnClickListener(v -> listener.onDeleteUser(user));
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onUserClicked(user);
            }
            return true;
        });

        holder.imgUserProfile.setOnClickListener(v -> {
            if (listener != null && user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                listener.onUserImageClicked(user, holder.imgUserProfile);
            }
        });
    }

    /**
     * Interface for listening to administrative actions on user accounts.
     */
    public interface OnUserActionListener {
        /**
         * Called when the administrative role toggle button is clicked.
         * @param user The {@link User} whose role is being modified.
         */
        void onToggleAdmin(User user);

        /**
         * Called when the user deletion button is clicked.
         * @param user The {@link User} account to be deleted.
         */
        void onDeleteUser(User user);

        /**
         * Called when a user row is long-clicked, typically for editing details.
         * @param user The {@link User} that was clicked.
         */
        void onUserClicked(User user);

        /**
         * Called when a user's profile image is clicked.
         * @param user The {@link User} whose image was clicked.
         * @param imageView The {@link ImageView} containing the profile image.
         */
        void onUserImageClicked(User user, ImageView imageView);
    }

    /**
     * ViewHolder for user account table rows.
     */
    public static class UserViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        /**
         * TextViews for name, email, role status, and birthdate.
         */
        final TextView txtUserFullName, txtUserEmail, txtUserIsAdmin, txtUserBirthDate;

        /**
         * Buttons for administrative actions.
         */
        final ImageButton btnDeleteUser, btnToggleAdmin;

        /**
         * ImageView for user profile picture.
         */
        final ImageView imgUserProfile;

        /**
         * Constructs a new UserViewHolder.
         * @param itemView The view representing a single user row.
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserFullName = itemView.findViewById(R.id.txt_UserRow_fullName);
            txtUserEmail = itemView.findViewById(R.id.txt_UserRow_email);
            txtUserBirthDate = itemView.findViewById(R.id.txt_UserRow_birthDate);
            txtUserIsAdmin = itemView.findViewById(R.id.txt_UserRow_IsAdmin);
            imgUserProfile = itemView.findViewById(R.id.img_UserRow_userProfile);
            btnDeleteUser = itemView.findViewById(R.id.btn_UserRow_DeleteUser);
            btnToggleAdmin = itemView.findViewById(R.id.btn_UserRow_MakeAdmin);
        }
    }
}