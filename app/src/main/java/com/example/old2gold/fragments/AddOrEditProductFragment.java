package com.example.fasta.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.fasta.R;
import com.example.fasta.enums.RecipeCategory;
import com.example.fasta.model.Model;
import com.example.fasta.model.Recipe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

public class AddOrEditProductFragment extends Fragment {
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private boolean isEditMode = false;
    private ProgressBar progressBar;
    TextInputEditText title;
    MaterialAutoCompleteTextView category;
    TextInputEditText description;
    Button saveBtn;
    ImageView productImage;
    FloatingActionButton camBtn;
    FloatingActionButton galleryBtn;
    Bitmap imageBitmap;
    String[] categories;
    String productId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_or_edit_product, container, false);
        this.productId = AddOrEditProductFragmentArgs.fromBundle(getArguments()).getProductId();
        this.isEditMode = productId != null;
                findAllElements(view);

        if (isEditMode) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Edit Product");
            saveBtn.setText("update");
            setProductDetails();
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Add Product");
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(title.getText())) {
                    title.setError("required field");
                }
                else {
                    save();
                }
            }
        });

        camBtn.setOnClickListener(v -> openCam());

        galleryBtn.setOnClickListener(v -> openGallery());
        this.getProductCategory(view);

        return view;
    }

    private void setProductDetails() {
        Model.instance.getProductById(productId, new Model.GetProductById() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(Recipe recipe) {
                title.setText(recipe.getTitle());
                category.setText(category.getAdapter().getItem(Arrays.stream(categories).collect(Collectors.toList()).indexOf(recipe.getProductCategory())).toString(),false);
                description.setText(recipe.getDescription());
                if (recipe.getImageUrl() != null) {
                    Picasso.get().load(recipe.getImageUrl()).into(productImage);
                }
            }
        });
    }

    private void findAllElements(View view) {
        title = view.findViewById(R.id.productTitle);
        category = view.findViewById(R.id.category);
        description = view.findViewById(R.id.description);
        saveBtn = view.findViewById(R.id.addProductBtn);
        productImage = view.findViewById(R.id.productImage);
        camBtn = view.findViewById(R.id.cameraBtn);
        galleryBtn = view.findViewById(R.id.galleryBtn);
        progressBar = view.findViewById(R.id.add_product_progressbar);
        progressBar.setVisibility(View.GONE);
    }

    private void getProductCategory(View view) {
        AutoCompleteTextView dropdown = view.findViewById(R.id.category);
        this.categories = new String[]{RecipeCategory.BAKING.toString(), RecipeCategory.COOKING.toString(), RecipeCategory.DESERT.toString(),
        RecipeCategory.OTHER.toString()};
        setDropdownAdapter(dropdown, categories);
    }

    private void setDropdownAdapter(AutoCompleteTextView dropdown, String[] items) {
        dropdown.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, items));
    }

    private void save() {
        saveBtn.setEnabled(false);
        camBtn.setEnabled(false);
        galleryBtn.setEnabled(false);

        Recipe recipe = getProduct(productId);
        saveProduct(recipe);
    }

    private void saveProduct(Recipe recipe) {
        progressBar.setVisibility(View.VISIBLE);

        if (imageBitmap == null) {
            Model.instance.saveProduct(recipe, () -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "saved product successfully!", Toast.LENGTH_LONG).show();
                Navigation.findNavController(this.title).navigateUp();
            });
        } else {
            Model.instance.saveProductImage(imageBitmap, UUID.randomUUID() + ".jpg", url -> {
                recipe.setImageUrl(url);
                Model.instance.saveProduct(recipe, () -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "saved product successfully!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(this.title).navigateUp();
                });
            });
        }
    }

    @NonNull
    private Recipe getProduct(String id) {
        final String key = id != null ? id : FirebaseDatabase.getInstance().getReference().push().getKey();
        return new Recipe(key,Objects.requireNonNull(this.title.getText()).toString(),
                this.description.getText() != null ? this.description.getText().toString() : "",
                "OK",
                this.isInArray(this.categories, this.category.getText().toString()) ? this.category.getText().toString() : RecipeCategory.OTHER.toString(),
                Model.instance.mAuth.getUid(),  false);
    }

    private boolean isInArray(String[] array, String string) {
        return Arrays.asList(array).contains(string);
    }

    private void openCam() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @SneakyThrows
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                productImage.setImageBitmap(imageBitmap);
            }

            if (requestCode == REQUEST_GALLERY) {
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null) {
                    productImage.setImageURI(selectedImageUri);
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), selectedImageUri);
                }
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
    }
}