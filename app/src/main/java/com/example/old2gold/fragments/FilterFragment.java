package com.example.old2gold.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.old2gold.R;
import com.example.old2gold.model.ProductFilterCB;
import com.example.old2gold.model.ProductListRvViewModel;

import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment {
    List<ProductFilterCB> types;
    MyProductTypesAdapter adapter;
    List<String> selectedStrings = new ArrayList<>();
    ProductListRvViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(ProductListRvViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_fragment, container, false);
        types = ProductFilterCB.getAllCheckboxTypes();

        RecyclerView list = view.findViewById(R.id.itemslist_rv);

        list.setHasFixedSize(true);

        list.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MyProductTypesAdapter();
        list.setAdapter(adapter);

        viewModel.addTypeFilter(new ArrayList<>());

        selectedStrings = new ArrayList<>();
        viewModel.addTypeFilter(selectedStrings);

        return view;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb;
        
        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            cb = itemView.findViewById(R.id.type_product_filter_cb);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedStrings.add(cb.getText().toString());
                    } else {
                        selectedStrings.remove(cb.getText().toString());
                    }

                    viewModel.addTypeFilter(selectedStrings);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    listener.onItemClick(v, pos);
                }
            });
        }
    }

    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    class MyProductTypesAdapter extends RecyclerView.Adapter<MyViewHolder> {
        OnItemClickListener listener;

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.product_type_checkbox, parent, false);
            MyViewHolder holder = new MyViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ProductFilterCB productFilterCB = types.get(position);
            holder.cb.setChecked(productFilterCB.isFlag());
            holder.cb.setText(productFilterCB.getProductType());
        }

        @Override
        public int getItemCount() {
            return types.size();
        }
    }
}
