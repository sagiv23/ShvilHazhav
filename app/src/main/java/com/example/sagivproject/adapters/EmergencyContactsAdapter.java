package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.EmergencyContact;

import javax.inject.Inject;

public class EmergencyContactsAdapter extends BaseAdapter<EmergencyContact, EmergencyContactsAdapter.EmergencyContactViewHolder> {
    private OnContactActionListener listener;

    @Inject
    public EmergencyContactsAdapter() {
    }

    public void setListener(OnContactActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmergencyContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new EmergencyContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyContactViewHolder holder, int position) {
        EmergencyContact contact = getItem(position);
        holder.txtName.setText(contact.getFullName());
        holder.txtPhone.setText(contact.getPhoneNumber());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(contact);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(contact);
        });
    }

    public interface OnContactActionListener {
        void onEdit(EmergencyContact contact);

        void onDelete(EmergencyContact contact);
    }

    public static class EmergencyContactViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final TextView txtPhone;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        public EmergencyContactViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_contact_name);
            txtPhone = itemView.findViewById(R.id.txt_contact_phone);
            btnEdit = itemView.findViewById(R.id.btn_edit_contact);
            btnDelete = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}
