package com.example.old2gold.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import com.example.old2gold.model.FavoriteProductListRvViewModel;
import com.example.old2gold.model.Model;
import com.example.old2gold.model.Recipe;
import com.example.old2gold.shared.CardViewHolder;
import com.example.old2gold.shared.OnItemClickListener;

public class MyProductsFragment extends Fragment {
    FavoriteProductListRvViewModel viewModel;
    MyAdapter adapter;
    SwipeRefreshLayout swipeRefresh;
    ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(FavoriteProductListRvViewModel.class);
    }

    @Nullable
    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liked_products,container,false);
        RecyclerView list = view.findViewById(R.id.like_products_itemslist_rv);
        progressBar = view.findViewById(R.id.favorite_products_progress_bar);
        progressBar.setVisibility(View.GONE);

        swipeRefresh = view.findViewById(R.id.likedproductslist_swiperefresh);
        swipeRefresh.setOnRefreshListener(() -> Model.instance.refreshProductsILikedByUserList());
        viewModel.refreshFavoriteItems();

        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyAdapter();
        list.setAdapter(adapter);
        setHasOptionsMenu(true);

        viewModel.getData().observe(getViewLifecycleOwner(), list1 -> refresh());

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v,int position) {
                String stId = viewModel.getData().getValue().get(position).getId();
                Navigation.findNavController(v).navigate(MyProductsFragmentDirections.navLikedProductsToNavProductDetails(stId));
            }
        });

        Model.instance.getFavoritesProductsLoadingState().observe(getViewLifecycleOwner(), favoriteLoadingState -> {
            if (favoriteLoadingState == Model.ProductsListLoadingState.loading) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });

        return view;
    }
    private void refresh() {
        adapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);
    }


    public class MyAdapter extends RecyclerView.Adapter<CardViewHolder> {
        OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener){
            this.listener = listener;
        }

        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_card, parent, false);
            CardViewHolder holder = new CardViewHolder(view, this.listener, getContext());
            return holder;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
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