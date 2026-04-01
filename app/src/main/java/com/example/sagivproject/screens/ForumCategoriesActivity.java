package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.ForumCategoryAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that displays a list of available forum categories.
 * <p>
 * This activity handles the retrieval and display of {@link ForumCategory} items.
 * It provides different functionality based on the user's role:
 * <ul>
 *     <li>Standard users can browse and select categories to view messages.</li>
 *     <li>Administrators can additionally add, rename, and delete categories.</li>
 * </ul>
 * It uses {@link ForumCategoryAdapter} to manage the list display.
 * </p>
 */
@AndroidEntryPoint
public class ForumCategoriesActivity extends BaseActivity {
    private ForumCategoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum_categories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forumCategoriesPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        User currentUser = sharedPreferencesUtil.getUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        TextView txtTitle = findViewById(R.id.txtForumCategoriesTitle);
        if (txtTitle != null) {
            txtTitle.setText(isAdmin ? R.string.forum_categories_options : R.string.forum_categories);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_forumCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = adapterService.getForumCategoryAdapter();
        adapter.init(new ForumCategoryAdapter.OnCategoryInteractionListener() {
            @Override
            public void onDelete(ForumCategory category) {
                if (isAdmin) deleteCategory(category);
            }

            @Override
            public void onEdit(ForumCategory category) {
                if (isAdmin) showEditDialog(category);
            }

            @Override
            public void onLongClick(ForumCategory category) {
                if (isAdmin) showEditDialog(category);
            }

            @Override
            public void onClick(ForumCategory category) {
                Intent intent = new Intent(ForumCategoriesActivity.this, ForumActivity.class);
                intent.putExtra("categoryId", category.getId());
                intent.putExtra("categoryName", category.getName());
                startActivity(intent);
            }
        }, isAdmin);
        recyclerView.setAdapter(adapter);

        View adminAddLayout = findViewById(R.id.layout_adminForumCategory_add_category);
        View adminAddBtn = findViewById(R.id.btn_adminForumCategory_add_category);
        EditText edtNewCategoryName = findViewById(R.id.edt_new_category_name);

        if (isAdmin) {
            if (adminAddLayout != null) adminAddLayout.setVisibility(View.VISIBLE);
            if (adminAddBtn != null) {
                adminAddBtn.setVisibility(View.VISIBLE);
                adminAddBtn.setOnClickListener(v -> {
                    String name = edtNewCategoryName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        addCategory(name, edtNewCategoryName);
                    }
                });
            }
        } else {
            if (adminAddLayout != null) adminAddLayout.setVisibility(View.GONE);
            if (adminAddBtn != null) {
                adminAddBtn.setVisibility(View.GONE);
            }
        }

        loadCategories();
    }

    /**
     * Fetches the current list of forum categories from the database.
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

    /**
     * Adds a new category to the database and refreshes the list.
     *
     * @param name     Name of the new category.
     * @param editText Reference to the input field to clear on success.
     */
    private void addCategory(String name, EditText editText) {
        databaseService.getForumCategoriesService().addCategory(name, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                editText.setText("");
                loadCategories();
                Toast.makeText(ForumCategoriesActivity.this, "קטגוריה נוספה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumCategoriesActivity.this, "שגיאה בהוספה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a dialog to rename an existing category.
     *
     * @param category The category to edit.
     */
    private void showEditDialog(ForumCategory category) {
        dialogService.showEditForumCategoryDialog(getSupportFragmentManager(), category, newName ->
                databaseService.getForumCategoriesService().updateCategoryName(category.getId(), newName, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void data) {
                        loadCategories();
                        Toast.makeText(ForumCategoriesActivity.this, "שם הקטגוריה עודכן", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(ForumCategoriesActivity.this, "שגיאה בעדכון שם הקטגוריה", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    /**
     * Deletes a category and all its messages from the database.
     *
     * @param category The category to remove.
     */
    private void deleteCategory(ForumCategory category) {
        databaseService.getForumCategoriesService().deleteCategory(category.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                adapter.removeCategory(category);
                Toast.makeText(ForumCategoriesActivity.this, "הקטגוריה נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumCategoriesActivity.this, "שגיאה במחיקת הקטגוריה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}