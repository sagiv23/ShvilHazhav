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

/**
 * A RecyclerView adapter for displaying a list of {@link ForumCategory} objects.
 * <p>
 * This adapter handles the binding of category data to the corresponding views.
 * It supports different appearances and actions based on whether the user is an admin,
 * such as showing a delete button for each category.
 * </p>
 */
public class ForumCategoryAdapter extends BaseAdapter<ForumCategory, ForumCategoryAdapter.CategoryViewHolder> {
    private final OnCategoryInteractionListener listener;
    private final boolean isAdmin;

    /**
     * Constructs a new ForumCategoryAdapter.
     *
     * @param listener The listener for category interaction events (click, delete).
     * @param isAdmin  True if the adapter should be in admin mode, false otherwise.
     */
    public ForumCategoryAdapter(@NonNull OnCategoryInteractionListener listener, boolean isAdmin) {
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    public void setCategories(List<ForumCategory> newCategories) {
        setData(newCategories);
    }

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

        // Configure visibility and actions based on admin status
        if (isAdmin) {
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

        holder.itemView.setOnClickListener(v -> listener.onClick(category));
    }

    /**
     * An interface for handling user interactions with a category item.
     */
    public interface OnCategoryInteractionListener {
        /**
         * Called when the delete button for a category is clicked.
         *
         * @param category The category to be deleted.
         */
        void onDelete(ForumCategory category);

        /**
         * Called when the edit button for a category is clicked.
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
         * Called when a category item is clicked.
         *
         * @param category The category that was clicked.
         */
        void onClick(ForumCategory category);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class CategoryViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView categoryName;
        final ImageButton deleteButton;
        final ImageButton editButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.txt_category_name);
            deleteButton = itemView.findViewById(R.id.btn_delete_category);
            editButton = itemView.findViewById(R.id.btn_edit_category);
        }
    }
}
