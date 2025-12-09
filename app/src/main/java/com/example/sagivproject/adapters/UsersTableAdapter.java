package com.example.sagivproject.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;

import java.util.List;

public class UsersTableAdapter extends RecyclerView.Adapter<UsersTableAdapter.UserViewHolder> {

    private final List<User> users;

    public UsersTableAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, @SuppressLint("RecyclerView") int position) {
        User user = users.get(position);

        holder.txtUserFullName.setText(user.getFullName());
        holder.txtUserEmail.setText(user.getEmail());
        holder.txtUserIsAdmin.setText("מנהל: " + (user.getIsAdmin() ? "כן" : "לא"));

        holder.btnDeleteUser.setOnClickListener(v -> {
            DatabaseService.getInstance().deleteUser(user.getUid(), new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    Toast.makeText(v.getContext(), "המשתמש נמחק", Toast.LENGTH_SHORT).show();
                    users.remove(position);
                    notifyItemRemoved(position);
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(v.getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserFullName, txtUserEmail, txtUserIsAdmin;
        ImageButton btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserFullName = itemView.findViewById(R.id.txtUserFullName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            txtUserIsAdmin = itemView.findViewById(R.id.txtUserIsAdmin);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
