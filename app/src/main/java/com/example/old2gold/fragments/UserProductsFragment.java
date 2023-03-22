package com.example.fasta.fragments;

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

import com.example.fasta.R;
import com.example.fasta.model.Model;
import com.example.fasta.model.Recipe;
import com.example.fasta.model.UserProductsListRvViewModel;
import com.example.fasta.shared.CardViewHolder;
import com.example.fasta.shared.OnItemClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class UserProductsFragment extends Fragment {
    private UserProductsListRvViewModel viewModel;
    private MyAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private String userId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(UserProductsListRvViewModel.class);
    }

    @Nullable
    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        userId = Model.instance.mAuth.getCurrentUser().getUid();
        View view = inflater.inflate(R.layout.fragment_my_products, container, false);
        RecyclerView list = view.findViewById(R.id.my_products_itemslist_rv);
        progressBar = view.findViewById(R.id.products_user_progress_bar);
        progressBar.setVisibility(View.GONE);
        swipeRefresh = view.findViewById(R.id.myproductslist_swiperefresh);
        swipeRefresh.setOnRefreshListener(() -> Model.instance.refreshProductsByMyUser());

        viewModel.refreshUserItems();



        FloatingActionButton add = view.findViewById(R.id.addProductButton);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v)
                        .navigate(MyProductsFragmentDirections.navMyProductsToNavAddProduct(null));
            }
        });


        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyAdapter();
        list.setAdapter(adapter);
        setHasOptionsMenu(true);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                String stId = viewModel.getData().getValue().get(position).getId();
                Navigation.findNavController(v).navigate(UserProductsFragmentDirections.navMyProductsToNavProductDetails(stId));
            }
        });

        viewModel.getData().observe(getViewLifecycleOwner(), list1 -> refresh());
        swipeRefresh.setRefreshing(Model.instance.getProductListLoadingState().getValue()
                == Model.ProductsListLoadingState.loading);

        Model.instance.getUserProductsLoadingState().observe(getViewLifecycleOwner(), userItemLoadingState -> {
            if (userItemLoadingState == Model.ProductsListLoadingState.loading) {
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

        public void setOnItemClickListener(OnItemClickListener listener) {
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

        // Replace the contents of a view (invoked by the layout manager)
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