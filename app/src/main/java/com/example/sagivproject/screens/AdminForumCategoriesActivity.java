package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
 * An activity for administrators to manage forum categories.
 * <p>
 * This screen allows administrators to view, add, and delete forum categories.
 * Categories are displayed in a RecyclerView.
 * </p>
 */
@AndroidEntryPoint
public class AdminForumCategoriesActivity extends BaseActivity {
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
        setContentView(R.layout.activity_admin_forum_categories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminForumCatergoriesPage), (v, insets) -> {
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
                deleteCategory(category);
            }

            @Override
            public void onLongClick(ForumCategory category) {
                ///TODO: implement
            }

            @Override
            public void onClick(ForumCategory category) {
                Intent intent = new Intent(AdminForumCategoriesActivity.this, AdminForumActivity.class);
                intent.putExtra("categoryId", category.getId());
                intent.putExtra("categoryName", category.getName());
                startActivity(intent);
            }
        }, true); // isAdmin = true
        recyclerView.setAdapter(adapter);

        EditText edtNewCategoryName = findViewById(R.id.edt_new_category_name);
        Button btnAddCategory = findViewById(R.id.btn_adminForumCategory_add_category);

        btnAddCategory.setOnClickListener(v -> {
            String categoryName = edtNewCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                databaseService.getForumCategoriesService().addCategory(categoryName, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void data) {
                        edtNewCategoryName.setText("");
                        loadCategories();
                        Toast.makeText(AdminForumCategoriesActivity.this, "קטגוריה נוספה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(AdminForumCategoriesActivity.this, "שגיאה בהוספה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
                adapter.submitList(data);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminForumCategoriesActivity.this, "שגיאה בטעינת קטגוריות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCategory(ForumCategory category) {
        databaseService.getForumCategoriesService().deleteCategory(category.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                loadCategories();
                Toast.makeText(AdminForumCategoriesActivity.this, "הקטגוריה נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AdminForumCategoriesActivity.this, "שגיאה במחיקת הקטגוריה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
