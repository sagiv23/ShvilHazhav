package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;

import java.util.List;

public class UsersTableAdapter extends RecyclerView.Adapter<UsersTableAdapter.UserViewHolder> {
    private final User currentUser;

    public interface OnUserActionListener {
        void onToggleAdmin(User user);
        void onDeleteUser(User user);
        void onUserClicked(User user);
    }

    private final List<User> users;
    private final OnUserActionListener listener;

    public UsersTableAdapter(List<User> users, User currentUser, OnUserActionListener listener) {
        this.users = users;
        this.currentUser = currentUser;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.txtUserFullName.setText(user.getFullName());
        holder.txtUserEmail.setText("אימייל: " + user.getEmail());
        holder.txtUserPassword.setText("סיסמה: " + user.getPassword());
        holder.txtUserIsAdmin.setText("מנהל: " + (user.getIsAdmin() ? "כן" : "לא"));
        holder.txtUserWins.setText("ניצחונות: " + user.getCountWins());

        boolean isSelf = user.equals(currentUser);

        if (isSelf) {
            holder.btnToggleAdmin.setVisibility(View.GONE);
        } else {
            holder.btnToggleAdmin.setVisibility(View.VISIBLE);
            holder.btnDeleteUser.setVisibility(View.VISIBLE);

            //שינוי אייקון לפי הסטטוס
            if (user.getIsAdmin()) {
                holder.btnToggleAdmin.setImageResource(R.drawable.ic_remove_admin);
                holder.btnToggleAdmin.setContentDescription("הסר מנהל");
            } else {
                holder.btnToggleAdmin.setImageResource(R.drawable.ic_add_admin);
                holder.btnToggleAdmin.setContentDescription("הפוך למנהל");
            }

            holder.btnToggleAdmin.setOnClickListener(v ->
                    listener.onToggleAdmin(user)
            );
        }

        holder.btnDeleteUser.setOnClickListener(v ->
                listener.onDeleteUser(user)
        );

        holder.itemView.setOnClickListener(v -> {
            if (!user.getIsAdmin()) {
                listener.onUserClicked(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserFullName, txtUserEmail, txtUserPassword, txtUserIsAdmin, txtUserWins;
        ImageButton btnDeleteUser, btnToggleAdmin;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserFullName = itemView.findViewById(R.id.txtUserFullName);
            txtUserEmail = itemView.findViewById(R.id.txt_UserRow_email);
            txtUserPassword = itemView.findViewById(R.id.txt_UserRow_password);
            txtUserIsAdmin = itemView.findViewById(R.id.txt_UserRow_IsAdmin);
            txtUserWins = itemView.findViewById(R.id.txt_UserRow_wins);
            btnDeleteUser = itemView.findViewById(R.id.btn_UserRow_DeleteUser);
            btnToggleAdmin = itemView.findViewById(R.id.btn_UserRow_MakeAdmin);
        }
    }
}