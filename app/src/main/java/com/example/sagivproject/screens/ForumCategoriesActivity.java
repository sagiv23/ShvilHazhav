package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.ForumCategoryAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity that displays a list of forum categories.
 * <p>
 * This screen allows users to view the available forum categories and navigate to a specific
 * forum by clicking on a category.
 * </p>
 */
@AndroidEntryPoint
public class ForumCategoriesActivity extends BaseActivity {
    private ForumCategoryAdapter adapter;

    /**
     * Initializes the activity, sets up the UI, and loads the forum categories.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum_categories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forumCategoriesPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        RecyclerView recyclerView = findViewById(R.id.recycler_forumCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ForumCategoryAdapter(new ForumCategoryAdapter.OnCategoryInteractionListener() {
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
                Intent intent = new Intent(ForumCategoriesActivity.this, ForumActivity.class);
                intent.putExtra("categoryId", category.getId());
                intent.putExtra("categoryName", category.getName());
                startActivity(intent);
            }
        }, false); // isAdmin = false
        recyclerView.setAdapter(adapter);

        loadCategories();
    }

    /**
     * Fetches the list of forum categories from the database and updates the RecyclerView.
     * Displays a toast message on failure.
     */
    private void loadCategories() {
        databaseService.getForumCategoriesService().getCategories(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumCategory> data) {
                adapter.setCategories(data);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumCategoriesActivity.this, "שגיאה בטעינת קטגוריות", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
