package com.example.old2gold.model;

import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class FavoriteProductListRvViewModel extends ViewModel {
    LiveData<List<Recipe>> data;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public FavoriteProductListRvViewModel() {
        data = Model.instance.getAllFavoriteProductsByUser();
    }

    public LiveData<List<Recipe>> getData() {
        return data;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void refreshFavoriteItems() {
        Model.instance.refreshProductsILikedByUserList();
    }
}
