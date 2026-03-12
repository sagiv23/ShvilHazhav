package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
 * An admin fragment for managing forum categories.
 */
@AndroidEntryPoint
public class AdminForumCategoriesFragment extends BaseFragment {
    private ForumCategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_forum_categories, container, false);
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
                deleteCategory(category);
            }

            @Override
            public void onEdit(ForumCategory category) {
                showEditDialog(category);
            }

            @Override
            public void onLongClick(ForumCategory category) {
                showEditDialog(category);
            }

            @Override
            public void onClick(ForumCategory category) {
                Bundle args = new Bundle();
                args.putString("categoryId", category.getId());
                args.putString("categoryName", category.getName());
                navigateTo(R.id.action_adminForumCategoriesFragment_to_adminForumFragment, args);
            }
        }, true);
        recyclerView.setAdapter(adapter);

        EditText edtNewCategoryName = view.findViewById(R.id.edt_new_category_name);
        view.findViewById(R.id.btn_adminForumCategory_add_category).setOnClickListener(v -> {
            String name = edtNewCategoryName.getText().toString().trim();
            if (!name.isEmpty()) {
                databaseService.getForumCategoriesService().addCategory(name, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void data) {
                        edtNewCategoryName.setText("");
                        loadCategories();
                        Toast.makeText(requireContext(), "קטגוריה נוספה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(requireContext(), "שגיאה בהוספה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
                Toast.makeText(requireContext(), "שגיאה בטעינת קטגוריות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(ForumCategory category) {
        if (getActivity() == null) return;
        dialogService.showEditForumCategoryDialog(getParentFragmentManager(), category, newName ->
                databaseService.getForumCategoriesService().updateCategoryName(category.getId(), newName, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void data) {
                        loadCategories();
                        Toast.makeText(requireContext(), "שם הקטגוריה עודכן", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(requireContext(), "שגיאה בעדכון שם הקטגוריה", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void deleteCategory(ForumCategory category) {
        databaseService.getForumCategoriesService().deleteCategory(category.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                adapter.removeCategory(category);
                Toast.makeText(requireContext(), "הקטגוריה נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה במחיקת הקטגוריה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
