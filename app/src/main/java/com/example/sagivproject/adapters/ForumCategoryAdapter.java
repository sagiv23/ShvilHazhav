package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ForumCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView adapter for displaying a list of {@link ForumCategory} objects.
 * <p>
 * This adapter handles the binding of category data to the corresponding views.
 * It supports different appearances and actions based on whether the user is an admin,
 * such as showing a delete button for each category.
 * It uses {@link DiffUtil} to efficiently update the list as category data changes.
 * </p>
 */
public class ForumCategoryAdapter extends RecyclerView.Adapter<ForumCategoryAdapter.CategoryViewHolder> {
    private final List<ForumCategory> categories = new ArrayList<>();
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

    /**
     * Updates the list of categories displayed by the adapter.
     * <p>
     * It uses {@link DiffUtil} to calculate the difference between the old and new lists,
     * and dispatches the update operations to the adapter, resulting in efficient updates
     * and animations.
     *
     * @param newCategories The new list of categories to display.
     */
    public void submitList(List<ForumCategory> newCategories) {
        GenericDiffCallback<ForumCategory> diffCallback = new GenericDiffCallback<>(this.categories, newCategories);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.categories.clear();
        this.categories.addAll(newCategories);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ForumCategory category = categories.get(position);
        holder.categoryName.setText(category.getName());

        // Configure visibility and actions based on admin status
        if (isAdmin) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> listener.onDelete(category));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
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
         * Called when a category item is clicked.
         *
         * @param category The category that was clicked.
         */
        void onClick(ForumCategory category);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView categoryName;
        final ImageButton deleteButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.txt_category_name);
            deleteButton = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
