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

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a table of {@link User} objects for administrative purposes.
 */
public class UsersTableAdapter extends BaseAdapter<User, UsersTableAdapter.UserViewHolder> {
    private final ImageUtil imageUtil;
    private User currentUser;
    private OnUserActionListener listener;

    @Inject
    public UsersTableAdapter(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    public void init(User currentUser, OnUserActionListener listener) {
        this.currentUser = currentUser;
        this.listener = listener;
    }

    public void setUserList(List<User> users) {
        setData(users);
    }

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
        holder.txtUserPassword.setText(String.format("סיסמה: %s", user.getPassword()));
        holder.txtUserAge.setText(MessageFormat.format("גיל: {0}", user.getAge()));
        holder.txtUserIsAdmin.setText(String.format("מנהל: %s", user.isAdmin() ? "כן" : "לא"));
        holder.txtUserWins.setText(MessageFormat.format("ניצחונות: {0}", user.getCountWins()));

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
            if (listener != null) {
                listener.onUserImageClicked(user, holder.imgUserProfile);
            }
        });
    }

    public interface OnUserActionListener {
        void onToggleAdmin(User user);

        void onDeleteUser(User user);

        void onUserClicked(User user);

        void onUserImageClicked(User user, ImageView imageView);
    }

    public static class UserViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
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
