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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Model {
    public final static Model instance = new Model();
    Executor executor = Executors.newFixedThreadPool(1);
    ModelFirebase modelFirebase = new ModelFirebase();
    MutableLiveData<List<Product>> productsList = new MutableLiveData<List<Product>>();
    MutableLiveData<List<Product>> favoriteProductsByUserList = new MutableLiveData<List<Product>>();
    MutableLiveData<List<Product>> productsByUserList = new MutableLiveData<List<Product>>();
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


    public LiveData<List<Product>> getAll() {
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
    public LiveData<List<Product>> getAllFavoriteProductsByUser() {
        if (favoriteProductsByUserList.getValue() == null) {
            refreshProductsILikedByUserList();
        }
        return favoriteProductsByUserList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LiveData<List<Product>> getProductOfUser() {
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

    public boolean isLoggedUser(Product product) {
        String loggedUserId = this.mAuth != null ? this.mAuth.getUid() : null;
        return product.contactId != null && product.contactId.equals(loggedUserId);
    }

    public boolean isInFilters(Product product) {
        List<String> categories = categoriesFilterList.getValue() != null ? categoriesFilterList.getValue() : new ArrayList<>();
        return categories.contains(product.productCategory) || categories.isEmpty();
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
                    List<Product> productList = AppLocalDb.db.productDao().getAll()
                            .stream().filter(product -> !isLoggedUser(product) && isInFilters(product))
                            .collect(Collectors.toList());
                    productsList.postValue(productList);
                    productListLoadingState.postValue(ProductsListLoadingState.loaded);
                }
            });
        });
    }

    private Long getProductsLastUpdateDate(Long lud, List<Product> allProducts) {
        for (Product product : allProducts) {
            AppLocalDb.db.productDao().insertAll(product);
            if (lud < product.getUpdateDate()) {
                lud = product.getUpdateDate();
            }
        }
        return lud;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void refreshProductsILikedByUserList() {
        favoriteProductsLoadingState.setValue(ProductsListLoadingState.loading);
        modelFirebase.getAllLikedProductsByUser(this.mAuth.getUid(), new ModelFirebase.GetLikedProductsListener() {
            @Override
            public void onComplete(List<Product> products) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Long lud = new Long(0);
                        Log.d("TAG", "fb returned " + products.size());
                        updateLastLocalUpdateDate(lud);
                        favoriteProductsByUserList.postValue(products);
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
                    List<Product> productList = AppLocalDb.db.productDao().getProductsByContactId(id);
                    productsByUserList.postValue(productList);
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


    public void saveProduct(Product product, AddProductListener listener) {
        modelFirebase.saveProduct(product, () -> {
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
        void onComplete(Product product);
    }

    public Product getProductById(String productId, GetProductById listener) {
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
