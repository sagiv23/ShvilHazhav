package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.ForumCategory;

import java.util.List;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a list of forum categories.
 * <p>
 * This adapter manages the display of {@link ForumCategory} items and dynamically
 * adapts its UI based on the user's role. Admins are provided with additional
 * buttons for editing and deleting categories directly from the list.
 * </p>
 */
public class ForumCategoryAdapter extends BaseAdapter<ForumCategory, ForumCategoryAdapter.CategoryViewHolder> {
    private OnCategoryInteractionListener listener;
    private boolean isAdmin;

    /**
     * Constructs a new ForumCategoryAdapter.
     * Use {@link #init(OnCategoryInteractionListener, boolean)} to configure after injection.
     */
    @Inject
    public ForumCategoryAdapter() {
    }

    /**
     * Initializes the adapter with a listener and role-based permissions.
     *
     * @param listener The listener for handling category interactions (clicks, edits, deletes).
     * @param isAdmin  true if the current user has administrative privileges.
     */
    public void init(OnCategoryInteractionListener listener, boolean isAdmin) {
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    /**
     * Updates the data set with a new list of categories.
     *
     * @param newCategories The list of {@link ForumCategory} objects to display.
     */
    public void setCategories(List<ForumCategory> newCategories) {
        setData(newCategories);
    }

    /**
     * Removes a specific category from the adapter's data set and notifies changes.
     *
     * @param category The category object to remove.
     */
    public void removeCategory(ForumCategory category) {
        int index = dataList.indexOf(category);
        if (index != -1) {
            dataList.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ForumCategory category = getItem(position);
        holder.categoryName.setText(category.getName());

        if (isAdmin && listener != null) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> listener.onDelete(category));
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> listener.onEdit(category));
            holder.itemView.setOnLongClickListener(v -> {
                listener.onLongClick(category);
                return true;
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onClick(category));
        }
    }

    /**
     * Interface for listening to user interactions with forum categories.
     */
    public interface OnCategoryInteractionListener {
        /**
         * Called when the delete button is clicked for a category.
         *
         * @param category The category to be deleted.
         */
        void onDelete(ForumCategory category);

        /**
         * Called when the edit button is clicked for a category.
         *
         * @param category The category to be edited.
         */
        void onEdit(ForumCategory category);

        /**
         * Called when a category item is long-clicked.
         *
         * @param category The category that was long-clicked.
         */
        void onLongClick(ForumCategory category);

        /**
         * Called when a category item is clicked normally.
         *
         * @param category The category that was clicked.
         */
        void onClick(ForumCategory category);
    }

    /**
     * ViewHolder for forum category items.
     */
    public static class CategoryViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        /**
         * TextView displaying the category name.
         */
        final TextView categoryName;

        /**
         * Button for deleting the category (Admin only).
         */
        final ImageButton deleteButton;

        /**
         * Button for editing the category (Admin only).
         */
        final ImageButton editButton;

        /**
         * Constructs a new CategoryViewHolder.
         *
         * @param itemView The item view for a category row.
         */
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.txt_category_name);
            deleteButton = itemView.findViewById(R.id.btn_delete_category);
            editButton = itemView.findViewById(R.id.btn_edit_category);
        }
    }
}
