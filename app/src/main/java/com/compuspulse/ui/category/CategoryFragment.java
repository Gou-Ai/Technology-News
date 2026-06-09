package com.compuspulse.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.compuspulse.R;
import com.compuspulse.data.remote.model.ProjectCategory;
import com.compuspulse.ui.detail.ArticleDetailActivity;
import com.compuspulse.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private Spinner spinnerCategory;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private CategoryArticleAdapter adapter;
    private CategoryViewModel viewModel;
    private final List<ProjectCategory> currentCategories = new ArrayList<>();

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initSpinner();
        initRecyclerView();
        initViewModel();
    }

    private void initView(View view) {
        spinnerCategory = view.findViewById(R.id.spinner_category);
        recyclerView = view.findViewById(R.id.recycler_view_category);
        tvEmpty = view.findViewById(R.id.tv_empty_category);
    }

    private void initSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < currentCategories.size()) {
                    viewModel.loadByCategory(currentCategories.get(position).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new CategoryArticleAdapter(article -> {
            Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
            intent.putExtra("article", article);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        viewModel.getCategoryList().observe(getViewLifecycleOwner(), categories -> {
            currentCategories.clear();
            if (categories != null) {
                currentCategories.addAll(categories);
            }
            List<String> names = new ArrayList<>();
            for (ProjectCategory category : currentCategories) {
                names.add(category.getName());
            }
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
            adapter.clear();
            adapter.addAll(names);
            adapter.notifyDataSetChanged();
            if (!currentCategories.isEmpty()) {
                spinnerCategory.setSelection(0, false);
            }
        });

        viewModel.getArticleList().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            boolean isEmpty = list == null || list.isEmpty();
            tvEmpty.setText("当前分类暂无内容");
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadCategories();
    }
}
