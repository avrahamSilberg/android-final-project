package com.example.fasta.model;

import static com.example.fasta.model.Recipe.PRODUCTS_COLLECTION_NAME;

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

public class ModelFirebase {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    public interface GetLikedProductsListener {
        void onComplete(List<Recipe> list);
    }

    public interface GetAllProductsListener {
        void onComplete(List<Recipe> list);
    }


    public interface GetMyProductsListener {
        void onComplete(List<Recipe> list);
    }

    public interface RemoveLikedProductsListener {
        void onComplete();
    }

    public interface AddLikedProductListener {
        void onComplete();
    }

    public void saveProduct(Recipe recipe, Model.AddProductListener listener) {
        Map<String, Object> json = recipe.toJson();
        db.collection(Recipe.PRODUCTS_COLLECTION_NAME)
                .document(recipe.getId())
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


    public List<Recipe> getAllProducts(Long lastUpdateDate, GetAllProductsListener listener) {
        List<Recipe> recipes = new ArrayList<>();
        db.collection(PRODUCTS_COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("updateDate", new Timestamp(lastUpdateDate, 0))
                .orderBy("updateDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createProductList(recipes, task);
                    }

                    listener.onComplete(recipes);
                });

        return recipes;
    }

    private void createProductList(List<Recipe> recipes, Task<QuerySnapshot> task) {
        for (DocumentSnapshot product : task.getResult()) {
            Recipe recipeToAdd = Recipe.create(Objects.requireNonNull(product.getData()));
            recipeToAdd.setId(product.getId());
            recipes.add(recipeToAdd);
        }
    }

    public void getProductById(String productId, Model.GetProductById listener) {
        db.collection(Recipe.PRODUCTS_COLLECTION_NAME)
                .document(productId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Recipe recipe = null;
                        if (task.isSuccessful() & task.getResult() != null) {
                            recipe = Recipe.create(task.getResult().getData());
                            recipe.setId(task.getResult().getId());
                        }
                        listener.onComplete(recipe);
                    }
                });
    }

    public List<Recipe> getProductsByUser(String id, GetMyProductsListener myProductsListener) {
        List<Recipe> recipes = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(PRODUCTS_COLLECTION_NAME)
                .whereEqualTo("contactId", id)
                .whereEqualTo("isDeleted", false)
                .orderBy("updateDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createProductList(recipes, task);
                    }
                    myProductsListener.onComplete(recipes);
                });

        return recipes;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Recipe> getAllLikedProductsByUser(String id, GetLikedProductsListener myProductsListener) {
        final User[] user = new User[1];
        List<Recipe> recipes = new ArrayList<>();

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

                                                                Recipe recipeToAdd = Recipe.create(Objects.requireNonNull(result.getData()));
                                                                recipeToAdd.setId(result.getId());

                                                                if (!recipeToAdd.isDeleted()) {
                                                                    recipes.add(recipeToAdd);
                                                                    myProductsListener.onComplete(recipes);
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            myProductsListener.onComplete(recipes);
                                        }
                                    }
                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }

                        });
        return recipes;
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

