package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.ForumCategoryAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.dialogs.EditForumCategoryDialog;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseCallback;
import com.example.sagivproject.services.IForumService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that displays a list of available forum categories.
 * <p>
 * This activity handles the retrieval and display of {@link ForumCategory} items.
 * It provides different functionality based on the user's role:
 * <ul>
 * <li>Standard users can browse and select categories to view messages.</li>
 * <li>Administrators can additionally add, rename, and delete categories.</li>
 * </ul>
 * It uses {@link ForumCategoryAdapter} to manage the list display.
 * </p>
 */
@AndroidEntryPoint
public class ForumCategoriesActivity extends BaseActivity {
    @Inject
    protected ForumCategoryAdapter adapter;

    @Inject
    protected IForumService forumService;

    @Inject
    protected Provider<EditForumCategoryDialog> editForumCategoryDialogProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_forum_categories, R.id.forumCategoriesPage);
        setupMenu();

        User currentUser = sharedPreferencesUtil.getUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();
        ((TextView) findViewById(R.id.txtForumCategoriesTitle)).setText(isAdmin ? R.string.forum_admin : R.string.forum_categories);

        RecyclerView recyclerView = findViewById(R.id.recycler_forumCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                onNavigate(new Intent(ForumCategoriesActivity.this, ForumActivity.class)
                        .putExtra("categoryId", category.getId())
                        .putExtra("categoryName", category.getName()));
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    /**
     * Fetches the current list of forum categories from the database.
     */
    private void loadCategories() {
        showLoading();
        forumService.getCategories(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumCategory> data) {
                hideLoading();
                adapter.setCategories(data);
                TextView txtNoCategories = findViewById(R.id.txt_no_categories);
                if (txtNoCategories != null) {
                    txtNoCategories.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
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
        showLoading();
        forumService.addCategory(name, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                hideLoading();
                editText.setText("");
                loadCategories();
                Toast.makeText(ForumCategoriesActivity.this, "קטגוריה נוספה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
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
        EditForumCategoryDialog dialog = editForumCategoryDialogProvider.get();
        dialog.setData(category, newName -> {
            showLoading();
            forumService.updateCategoryName(category.getId(), newName, new DatabaseCallback<>() {
                @Override
                public void onCompleted(Void data) {
                    hideLoading();
                    loadCategories();
                    Toast.makeText(ForumCategoriesActivity.this, "שם הקטגוריה עודכן", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(Exception e) {
                    hideLoading();
                    Toast.makeText(ForumCategoriesActivity.this, "שגיאה בעדכון שם הקטגוריה", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "EditForumCategoryDialog");
    }

    /**
     * Deletes a category and all its messages from the database.
     *
     * @param category The category to remove.
     */
    private void deleteCategory(ForumCategory category) {
        showLoading();
        forumService.deleteCategory(category.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                hideLoading();
                adapter.removeCategory(category);
                Toast.makeText(ForumCategoriesActivity.this, "הקטגוריה נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                hideLoading();
                Toast.makeText(ForumCategoriesActivity.this, "שגיאה במחיקת הקטגוריה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}