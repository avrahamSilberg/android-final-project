package com.example.old2gold.model;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Set;
import java.util.logging.Filter;

public class ProductListRvViewModel extends ViewModel {
    LiveData<List<Product>> filteredProducts;
    LiveData<List<Product>> originalProducts;
    LiveData<List<Product>> categoriesFilterList;

    private final MutableLiveData<Set<Filter>> filters = new MutableLiveData<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addTypeFilter(List<String> categories) {
        Model.instance.getProductListByTypeFilter(categories);
    }

    public ProductListRvViewModel() {
        LiveData<List<Product>> all = Model.instance.getAll();
        filteredProducts = all;
        originalProducts = all;
    }

    public void refreshProducts() {
        Model.instance.refreshProductsList();
    }

    public LiveData<List<Product>> getData() {
        return filteredProducts;
    }
}
