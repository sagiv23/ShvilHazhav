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
        holder.txtUserEmail.setText(user.getEmail());
        holder.txtUserIsAdmin.setText("מנהל: " + (user.getIsAdmin() ? "כן" : "לא"));

        boolean isSelf = user.equals(currentUser);

        if (isSelf) {
            holder.btnToggleAdmin.setVisibility(View.GONE);
        } else {
            holder.btnToggleAdmin.setVisibility(View.VISIBLE);
            holder.btnToggleAdmin.setOnClickListener(v -> listener.onToggleAdmin(user));
        }

        // כפתור החלפת מנהל
        holder.btnToggleAdmin.setOnClickListener(v ->
                listener.onToggleAdmin(user)
        );

        // כפתור מחיקה
        holder.btnDeleteUser.setOnClickListener(v ->
                listener.onDeleteUser(user)
        );
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserFullName, txtUserEmail, txtUserIsAdmin;
        ImageButton btnDeleteUser, btnToggleAdmin;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserFullName = itemView.findViewById(R.id.txtUserFullName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            txtUserIsAdmin = itemView.findViewById(R.id.txtUserIsAdmin);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
            btnToggleAdmin = itemView.findViewById(R.id.btnMakeAdmin);
        }
    }
}