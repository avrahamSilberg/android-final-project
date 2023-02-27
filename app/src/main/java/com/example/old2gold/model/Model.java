package com.example.old2gold.model;

import static com.example.old2gold.MyApplication.getContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.old2gold.MyApplication;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Model {
    public final static Model instance = new Model();
    Executor executor = Executors.newFixedThreadPool(1);
    ModelFirebase modelFirebase = new ModelFirebase();
    MutableLiveData<List<Recipe>> productsList = new MutableLiveData<List<Recipe>>();
    MutableLiveData<List<Recipe>> favoriteProductsByUserList = new MutableLiveData<List<Recipe>>();
    MutableLiveData<List<Recipe>> productsByUserList = new MutableLiveData<List<Recipe>>();
    MutableLiveData<ProductsListLoadingState> productListLoadingState = new MutableLiveData<ProductsListLoadingState>();
    MutableLiveData<ProductsListLoadingState> userProductsLoadingState = new MutableLiveData<ProductsListLoadingState>();
    MutableLiveData<ProductsListLoadingState> favoriteProductsLoadingState = new MutableLiveData<ProductsListLoadingState>();
    MutableLiveData<List<String>> categoriesFilterList = new MutableLiveData<>();
    MutableLiveData<User> loggedUser = new MutableLiveData<User>();

    public FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public enum ProductsListLoadingState {
        loading,
        loaded
    }

    public interface AddProductListener {
        void onComplete();
    }

    public interface GetLoggedUserListener {
        void onComplete(User user);
    }

    public interface UpdateDataListener {
        void onComplete();
    }

    public interface SaveImageListener {
        void onComplete(String url);

    }

    public LiveData<ProductsListLoadingState> getProductListLoadingState() {
        return productListLoadingState;
    }

    public LiveData<ProductsListLoadingState> getUserProductsLoadingState() {
        return userProductsLoadingState;
    }

    public LiveData<ProductsListLoadingState> getFavoritesProductsLoadingState() {
        return favoriteProductsLoadingState;
    }

    private Model() {
        productListLoadingState.setValue(ProductsListLoadingState.loaded);
        userProductsLoadingState.setValue(ProductsListLoadingState.loaded);
        favoriteProductsLoadingState.setValue(ProductsListLoadingState.loaded);
    }


    public LiveData<List<Recipe>> getAll() {
        if (productsList.getValue() == null) {
            categoriesFilterList.postValue(new ArrayList<>());
            refreshProductsList();
        }

        return productsList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LiveData<User> getLoggedUser() {
        if (loggedUser.getValue() == null) {
            refreshLoggedUser();
        }

        return loggedUser;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LiveData<List<Recipe>> getAllFavoriteProductsByUser() {
        if (favoriteProductsByUserList.getValue() == null) {
            refreshProductsILikedByUserList();
        }
        return favoriteProductsByUserList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LiveData<List<Recipe>> getProductOfUser() {
        if (productsByUserList.getValue() == null) {
            refreshProductsByMyUser();
        }

        return productsByUserList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getProductListByTypeFilter(List<String> selectedCategories) {
        categoriesFilterList.postValue(selectedCategories);
        refreshProductsList();
    }

    public boolean isLoggedUser(Recipe recipe) {
        String loggedUserId = this.mAuth != null ? this.mAuth.getUid() : null;
        return recipe.contactId != null && recipe.contactId.equals(loggedUserId);
    }

    public boolean isInFilters(Recipe recipe) {
        List<String> categories = categoriesFilterList.getValue() != null ? categoriesFilterList.getValue() : new ArrayList<>();
        return categories.contains(recipe.productCategory) || categories.isEmpty();
    }

    public void refreshProductsList() {
        productListLoadingState.setValue(ProductsListLoadingState.loading);
        Long lastUpdateDate = MyApplication.getContext().getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong("lastUpdate", 0);
        modelFirebase.getAllProducts(lastUpdateDate, allProducts -> {
            executor.execute(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    Long lud = new Long(0);
                    Log.d("TAG", "fb returned " + allProducts.size());
                    lud = getProductsLastUpdateDate(lud, allProducts);
                    updateLastLocalUpdateDate(lud);
                    List<Recipe> recipeList = AppLocalDb.db.productDao().getAll().stream().filter(recipe -> isInFilters(recipe)).collect(Collectors.toList());
                    productsList.postValue(recipeList);
                    productListLoadingState.postValue(ProductsListLoadingState.loaded);
                }
            });
        });
    }

    private Long getProductsLastUpdateDate(Long lud, List<Recipe> allRecipes) {
        for (Recipe recipe : allRecipes) {
            AppLocalDb.db.productDao().insertAll(recipe);
            if (lud < recipe.getUpdateDate()) {
                lud = recipe.getUpdateDate();
            }
        }
        return lud;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void refreshProductsILikedByUserList() {
        favoriteProductsLoadingState.setValue(ProductsListLoadingState.loading);
        modelFirebase.getAllLikedProductsByUser(this.mAuth.getUid(), new ModelFirebase.GetLikedProductsListener() {
            @Override
            public void onComplete(List<Recipe> recipes) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Long lud = new Long(0);
                        Log.d("TAG", "fb returned " + recipes.size());
                        updateLastLocalUpdateDate(lud);
                        favoriteProductsByUserList.postValue(recipes);
                        favoriteProductsLoadingState.postValue(ProductsListLoadingState.loaded);
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void refreshProductsByMyUser() {
        userProductsLoadingState.setValue(ProductsListLoadingState.loading);
        String id = this.mAuth.getUid();
        modelFirebase.getProductsByUser(id, products -> {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Long lastUpdateDate = new Long(0);
                    Log.d("TAG", "fb returned " + products.size());
                    updateLastLocalUpdateDate(getProductsLastUpdateDate(lastUpdateDate, products));
                    List<Recipe> recipeList = AppLocalDb.db.productDao().getProductsByContactId(id);
                    productsByUserList.postValue(recipeList);
                    userProductsLoadingState.postValue(ProductsListLoadingState.loaded);
                }
            });
        });
    }

    private void updateLastLocalUpdateDate(Long lastUpdateDate) {
        getContext()
                .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                .edit()
                .putLong("updateDate", lastUpdateDate)
                .commit();
    }


    public void saveProduct(Recipe recipe, AddProductListener listener) {
        modelFirebase.saveProduct(recipe, () -> {
            listener.onComplete();
            refreshProductsList();
        });
    }

    public void saveProductImage(Bitmap imageBitmap, String imageName, SaveImageListener listener) {
        modelFirebase.saveImage(imageBitmap, imageName, listener, "products");
    }

    public void saveUserImage(Bitmap imageBitmap, String imageName, SaveImageListener listener) {
        modelFirebase.saveImage(imageBitmap, imageName, listener, "users");
    }

    public void saveUser(User user, String id) {
        modelFirebase.addUser(user, id);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateUser(User user, UpdateDataListener listener) {
        modelFirebase.updateUser(user, listener);

        refreshLoggedUser();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public User getUser(String id, Model.GetLoggedUserListener optionalListener) {
        return modelFirebase.getUser(id, optionalListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public User refreshLoggedUser() {
        return modelFirebase.getUserO(mAuth.getUid(), user -> {
            loggedUser.postValue(user);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public User getProductSellerUser(String id, Model.GetLoggedUserListener optionalListener) {
        return modelFirebase.getProductSellerUser(id, optionalListener);
    }

    public interface GetProductById {
        void onComplete(Recipe recipe);
    }

    public Recipe getProductById(String productId, GetProductById listener) {
        modelFirebase.getProductById(productId, listener);
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void removeFromLikedProducts(String productId, ModelFirebase.RemoveLikedProductsListener listener) {
        modelFirebase.removeFromFavoriteList(productId, () -> {
            listener.onComplete();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addToLikedProducts(String productId, ModelFirebase.AddLikedProductListener likedProductListener) {
        modelFirebase.addToLikedProducts(productId, () -> {
            likedProductListener.onComplete();
        });
    }


}
