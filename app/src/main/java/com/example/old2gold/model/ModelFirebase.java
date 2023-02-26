package com.example.old2gold.model;

import static com.example.old2gold.model.Product.PRODUCTS_COLLECTION_NAME;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ModelFirebase {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public interface GetAllProductsListener {
        void onComplete(List<Product> list);
    }

    public interface GetLikedProductsListener {
        void onComplete(List<Product> list);
    }

    public interface GetMyProductsListener {
        void onComplete(List<Product> list);
    }


    public interface RemoveLikedProductsListener {
        void onComplete();
    }

    public interface AddLikedProductListener {
        void onComplete();
    }

    public void saveProduct(Product product, Model.AddProductListener listener) {
        Map<String, Object> json = product.toJson();
        db.collection(Product.PRODUCTS_COLLECTION_NAME)
                .document(product.getId())
                .set(json)
                .addOnSuccessListener(unused -> listener.onComplete())
                .addOnFailureListener(e -> listener.onComplete());
    }

    public void saveImage(Bitmap imageBitmap, String imageName, Model.SaveImageListener listener, String directory) {
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child(directory + "/" + imageName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imgRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> listener.onComplete(null))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            listener.onComplete(uri.toString());
                        });
                    }
                });
    }

    public void addUser(User user, String id) {
        Map<String, Object> json = user.toJson();
        db.collection(User.COLLECTION_NAME)
                .document(id)
                .set(json);
    }

    public void updateUser(User updatedUser, Model.UpdateDataListener listener) {
        Map<String, Object> json = updatedUser.toJson();
        db.collection(User.COLLECTION_NAME)
                .document(updatedUser.getId())
                .set(json)
                .addOnCompleteListener(unused -> listener.onComplete());

    }

    public User getUser(String id, Model.GetLoggedUserListener optionalListener) {
        final User[] user = new User[1];

        if (id != null) {
            DocumentReference docRef = db.collection(User.COLLECTION_NAME)
                    .document(id);
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    user[0] = document.toObject(User.class);
                                    user[0].setId(document.getId());


                                    optionalListener.onComplete((user[0]));
                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                        }
                    });
        }

        return user[0];
    }

    public User getProductSellerUser(String id, Model.GetLoggedUserListener optionalListener) {
        final User[] user = new User[1];

        DocumentReference docRef = db.collection(User.COLLECTION_NAME)
                .document(id);
        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                user[0] = document.toObject(User.class);
                                user[0].setId(document.getId());


                            } else {
                                Log.d("TAG", "No such document");
                            }

                            optionalListener.onComplete((user[0]));
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });

        return user[0];
    }

    public User getUserO(String id, Model.GetLoggedUserListener optionalListener) {
        final User[] user = new User[1];

        DocumentReference docRef = db.collection(User.COLLECTION_NAME)
                .document(id);
        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                user[0] = document.toObject(User.class);
                                user[0].setId(document.getId());


                            } else {
                                Log.d("TAG", "No such document");
                            }

                            optionalListener.onComplete((user[0]));

                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });

        return user[0];
    }


    public List<Product> getAllProducts(Long lastUpdateDate, GetAllProductsListener listener) {
        List<Product> products = new ArrayList<>();
        db.collection(PRODUCTS_COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("updateDate", new Timestamp(lastUpdateDate, 0))
                .orderBy("updateDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createProductList(products, task);
                    }

                    listener.onComplete(products);
                });

        return products;
    }

    private void createProductList(List<Product> products, Task<QuerySnapshot> task) {
        for (DocumentSnapshot product : task.getResult()) {
            Product productToAdd = Product.create(Objects.requireNonNull(product.getData()));
            productToAdd.setId(product.getId());
            products.add(productToAdd);
        }
    }

    public void getProductById(String productId, Model.GetProductById listener) {
        db.collection(Product.PRODUCTS_COLLECTION_NAME)
                .document(productId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Product product = null;
                        if (task.isSuccessful() & task.getResult() != null) {
                            product = Product.create(task.getResult().getData());
                            product.setId(task.getResult().getId());
                        }
                        listener.onComplete(product);
                    }
                });
    }

    public List<Product> getProductsByUser(String id, GetMyProductsListener myProductsListener) {
        List<Product> products = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(PRODUCTS_COLLECTION_NAME)
                .whereEqualTo("contactId", id)
                .whereEqualTo("isDeleted", false)
                .orderBy("updateDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createProductList(products, task);
                    }
                    myProductsListener.onComplete(products);
                });

        return products;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Product> getAllLikedProductsByUser(String id, GetLikedProductsListener myProductsListener) {
        final User[] user = new User[1];
        List<Product> products = new ArrayList<>();

        db.collection(User.COLLECTION_NAME)
                .document(id)
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    user[0] = document.toObject(User.class);


                                    ArrayList<String> favoriteProducts = user[0].getFavoriteProducts();

                                    if (favoriteProducts != null) {
                                        if (favoriteProducts.stream().count() != 0) {
                                            for (int i = 0; i < favoriteProducts.size(); i++) {
                                                FirebaseFirestore.getInstance().collection(PRODUCTS_COLLECTION_NAME)
                                                        .document(favoriteProducts.get(i))
                                                        .get()
                                                        .addOnCompleteListener(productsTask -> {
                                                            if (productsTask.isSuccessful()) {
                                                                DocumentSnapshot result = productsTask.getResult();

                                                                Product productToAdd = Product.create(Objects.requireNonNull(result.getData()));
                                                                productToAdd.setId(result.getId());

                                                                if (!productToAdd.isDeleted()) {
                                                                    products.add(productToAdd);
                                                                    myProductsListener.onComplete(products);
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            myProductsListener.onComplete(products);
                                        }
                                    }
                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }

                        });
        return products;
    }

    public void addToLikedProducts(String productId, AddLikedProductListener addLikedProductListener) {
        String userId = Model.instance.mAuth.getUid();

        db.collection(User.COLLECTION_NAME)
                .document(userId)
                .update("favoriteProducts", FieldValue.arrayUnion(productId))
                .addOnSuccessListener(unused -> addLikedProductListener.onComplete());
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void removeFromFavoriteList(String productId, RemoveLikedProductsListener removeLikedProductsListener) {
        String userId = Model.instance.mAuth.getUid();

        db.collection(User.COLLECTION_NAME)
                .document(userId)
                .update("favoriteProducts", FieldValue.arrayRemove(productId))
                .addOnSuccessListener(unused -> removeLikedProductsListener.onComplete());
    }


}

