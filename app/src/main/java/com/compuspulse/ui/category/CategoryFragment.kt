package com.compuspulse.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.compuspulse.R
import com.compuspulse.data.remote.model.ProjectCategory
import com.compuspulse.ui.detail.ArticleDetailActivity
import com.compuspulse.viewmodel.CategoryViewModel

class CategoryFragment : Fragment() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: CategoryArticleAdapter
    private val viewModel: CategoryViewModel by viewModels()
    private val currentCategories: MutableList<ProjectCategory> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initSpinner()
        initRecyclerView()
        initViewModel()
    }

    private fun initView(view: View) {
        spinnerCategory = view.findViewById(R.id.spinner_category)
        recyclerView = view.findViewById(R.id.recycler_view_category)
        tvEmpty = view.findViewById(R.id.tv_empty_category)
    }

    private fun initSpinner() {
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            ArrayList<String>()
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < currentCategories.size) {
                    viewModel.loadByCategory(currentCategories[position].getId())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        adapter = CategoryArticleAdapter { article ->
            val intent = Intent(requireContext(), ArticleDetailActivity::class.java)
            intent.putExtra("article", article)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun initViewModel() {
        viewModel.getCategoryList().observe(viewLifecycleOwner) { categories ->
            currentCategories.clear()
            categories?.let { currentCategories.addAll(it) }
            val names = ArrayList<String>()
            for (category in currentCategories) {
                names.add(category.getName())
            }
            @Suppress("UNCHECKED_CAST")
            val spinnerAdapter = spinnerCategory.adapter as ArrayAdapter<String>
            spinnerAdapter.clear()
            spinnerAdapter.addAll(names)
            spinnerAdapter.notifyDataSetChanged()
            if (currentCategories.isNotEmpty()) {
                spinnerCategory.setSelection(0, false)
            }
        }

        viewModel.getArticleList().observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val isEmpty = list.isNullOrEmpty()
            tvEmpty.text = "当前分类暂无内容"
            tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        viewModel.getErrorMessage().observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadCategories()
    }

    companion object {
        @JvmStatic
        fun newInstance(): CategoryFragment = CategoryFragment()
    }
}
