package com.example.fasta.model;

import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class UserProductsListRvViewModel extends ViewModel {
    LiveData<List<Recipe>> data;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public UserProductsListRvViewModel() {
        data = Model.instance.getProductOfUser();
    }

    public LiveData<List<Recipe>> getData() {
        return data;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void refreshUserItems() {
        Model.instance.refreshProductsByMyUser();
    }
}