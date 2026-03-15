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
 * This adapter handles the display of {@link ForumCategory} items and manages
 * different behaviors based on whether the current user is an administrator.
 * Admins are provided with edit and delete options for each category.
 * </p>
 */
public class ForumCategoryAdapter extends BaseAdapter<ForumCategory, ForumCategoryAdapter.CategoryViewHolder> {
    private OnCategoryInteractionListener listener;
    private boolean isAdmin;

    /**
     * Constructs a new ForumCategoryAdapter.
     */
    @Inject
    public ForumCategoryAdapter() {
    }

    /**
     * Initializes the adapter with a listener and the admin status.
     *
     * @param listener The listener for handling category interactions.
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
     * Removes a specific category from the adapter's data set.
     *
     * @param category The category to remove.
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

        // Configure admin-specific controls
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
         * Called when the delete button is clicked.
         */
        void onDelete(ForumCategory category);

        /**
         * Called when the edit button is clicked.
         */
        void onEdit(ForumCategory category);

        /**
         * Called when a category item is long-clicked.
         */
        void onLongClick(ForumCategory category);

        /**
         * Called when a category item is clicked.
         */
        void onClick(ForumCategory category);
    }

    /**
     * ViewHolder for forum category items.
     */
    public static class CategoryViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView categoryName;
        final ImageButton deleteButton;
        final ImageButton editButton;

        /**
         * Constructs a new CategoryViewHolder.
         *
         * @param itemView The item view.
         */
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.txt_category_name);
            deleteButton = itemView.findViewById(R.id.btn_delete_category);
            editButton = itemView.findViewById(R.id.btn_edit_category);
        }
    }
}
