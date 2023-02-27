package com.example.old2gold.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.old2gold.R;
import com.example.old2gold.model.Model;
import com.example.old2gold.model.Recipe;
import com.example.old2gold.model.ProductListRvViewModel;
import com.example.old2gold.shared.CardViewHolder;
import com.example.old2gold.shared.OnItemClickListener;

public class HomeFragment extends Fragment {
    ProductListRvViewModel viewModel;
    MyAdapter adapter;
    SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(ProductListRvViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefresh = view.findViewById(R.id.productslist_swiperefresh);
        swipeRefresh.setOnRefreshListener(() -> Model.instance.refreshProductsList());

        progressBar = view.findViewById(R.id.products_home_progress_bar);
        progressBar.setVisibility(View.GONE);
        viewModel.refreshProducts();

        RecyclerView productList = view.findViewById(R.id.productslist_rv);
        Button categoryFilterButton = view.findViewById(R.id.category_filter_bv);

        productList.setHasFixedSize(true);
        productList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyAdapter();
        productList.setAdapter(adapter);
        setHasOptionsMenu(true);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                String productId = viewModel.getData().getValue().get(position).getId();
                Navigation.findNavController(v).navigate(HomeFragmentDirections.navHomeToNavProductDetails(productId));
            }
        });

        ToggleCategoriesFilter(view, categoryFilterButton);

        viewModel.getData().observe(getViewLifecycleOwner(), list1 -> refresh());

        Model.instance.getProductListLoadingState().observe(getViewLifecycleOwner(), productsListLoadingState -> {
            if (productsListLoadingState == Model.ProductsListLoadingState.loading) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }

        });

        return view;
    }

    private void ToggleCategoriesFilter(View view, Button categoryFilterButton) {
        categoryFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollView typeScrollView = view.findViewById(R.id.type_filters_scroll_view);
                typeScrollView.setVisibility(typeScrollView.getVisibility() == View.GONE ?
                        View.VISIBLE : View.GONE);
            }
        });
    }

    private void refresh() {
        adapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);
    }

    public class MyAdapter extends RecyclerView.Adapter<CardViewHolder> {
        OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_card, parent, false);
            CardViewHolder holder = new CardViewHolder(view, listener, getContext());
            return holder;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
            Recipe recipe = viewModel.getData().getValue().get(position);
            holder.bind(recipe);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getData().getValue() == null) {
                return 0;
            }
            return viewModel.getData().getValue().size();
        }


    }
}