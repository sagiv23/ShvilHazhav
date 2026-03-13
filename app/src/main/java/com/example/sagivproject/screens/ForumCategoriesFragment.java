package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.ForumCategoryAdapter;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment that displays a list of forum categories.
 */
@AndroidEntryPoint
public class ForumCategoriesFragment extends BaseFragment {
    private ForumCategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forum_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_forumCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = adapterService.getForumCategoryAdapter();
        adapter.init(new ForumCategoryAdapter.OnCategoryInteractionListener() {
            @Override
            public void onDelete(ForumCategory category) {
                // Not used in this activity
            }

            @Override
            public void onEdit(ForumCategory category) {
                // Not used in this activity
            }

            @Override
            public void onLongClick(ForumCategory category) {
                // Not used in this activity
            }

            @Override
            public void onClick(ForumCategory category) {
                // Using Safe Args Directions
                ForumCategoriesFragmentDirections.ActionForumCategoriesFragmentToForumFragment action =
                        ForumCategoriesFragmentDirections.actionForumCategoriesFragmentToForumFragment(category.getId(), category.getName());
                navigateTo(action);
            }
        }, false); // isAdmin = false
        recyclerView.setAdapter(adapter);

        loadCategories();
    }

    private void loadCategories() {
        databaseService.getForumCategoriesService().getCategories(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumCategory> data) {
                adapter.setCategories(data);
            }

            @Override
            public void onFailed(Exception e) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "שגיאה בטעינת קטגוריות", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
