package com.example.old2gold.model;

import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class UserProductsListRvViewModel extends ViewModel {
    LiveData<List<Product>> data;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public UserProductsListRvViewModel() {
        data = Model.instance.getProductOfUser();
    }

    public LiveData<List<Product>> getData() {
        return data;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void refreshUserItems() {
        Model.instance.refreshProductsByMyUser();
    }
}