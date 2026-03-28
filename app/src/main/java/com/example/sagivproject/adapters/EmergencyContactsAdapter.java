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

/**
 * Adapter for displaying emergency contacts in a RecyclerView.
 * <p>
 * This adapter handles the visual representation of {@link EmergencyContact} objects and
 * provides interactive buttons for editing and deleting contacts through a callback listener.
 * </p>
 */
public class EmergencyContactsAdapter extends BaseAdapter<EmergencyContact, EmergencyContactsAdapter.EmergencyContactViewHolder> {
    private OnContactActionListener listener;

    /**
     * Constructs a new EmergencyContactsAdapter.
     * Hilt-compatible constructor.
     */
    @Inject
    public EmergencyContactsAdapter() {
    }

    /**
     * Sets the listener for contact-specific actions (edit and delete).
     *
     * @param listener The {@link OnContactActionListener} to handle user interactions.
     */
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

    /**
     * Interface for listening to user actions on emergency contacts.
     */
    public interface OnContactActionListener {
        /**
         * Called when the edit button is clicked for a specific contact.
         *
         * @param contact The {@link EmergencyContact} to edit.
         */
        void onEdit(EmergencyContact contact);

        /**
         * Called when the delete button is clicked for a specific contact.
         *
         * @param contact The {@link EmergencyContact} to delete.
         */
        void onDelete(EmergencyContact contact);
    }

    /**
     * ViewHolder for emergency contact items.
     */
    public static class EmergencyContactViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView displaying the contact's full name.
         */
        final TextView txtName;

        /**
         * TextView displaying the contact's phone number.
         */
        final TextView txtPhone;

        /**
         * ImageButton for editing the contact.
         */
        final ImageButton btnEdit;

        /**
         * ImageButton for deleting the contact.
         */
        final ImageButton btnDelete;

        /**
         * Constructs a new EmergencyContactViewHolder.
         *
         * @param itemView The view representing a single contact row.
         */
        public EmergencyContactViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_contact_name);
            txtPhone = itemView.findViewById(R.id.txt_contact_phone);
            btnEdit = itemView.findViewById(R.id.btn_edit_contact);
            btnDelete = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}
