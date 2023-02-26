package com.example.old2gold.fragments;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.old2gold.R;
import com.example.old2gold.enums.Gender;
import com.example.old2gold.enums.ProductCategory;
import com.example.old2gold.enums.ProductCondition;
import com.example.old2gold.model.Model;
import com.example.old2gold.model.Product;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean isEditMode = false;
    private LatLng currLocation;
    private ProgressBar progressBar;
    TextInputEditText title;
    MaterialAutoCompleteTextView gender;
    MaterialAutoCompleteTextView category;
    MaterialAutoCompleteTextView condition;
    TextInputEditText description;
    TextInputLayout price;
    Button saveBtn;
    ImageView productImage;
    FloatingActionButton camBtn;
    FloatingActionButton galleryBtn;
    Bitmap imageBitmap;
    String[] categories;
    String[] genders;
    String[] states;
    String productId;
    Boolean isSold = false;

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
                if(TextUtils.isEmpty(title.getText())) {
                    price.setError("required field");
                }
                else {
                    save();
                }
            }
        });

        camBtn.setOnClickListener(v -> openCam());

        galleryBtn.setOnClickListener(v -> openGallery());

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1337);
        this.getStateDropdown(view);
        this.getGenderDropdown(view);
        this.getProductCategory(view);
        this.getCurrentLocation();

        return view;
    }

    private void setProductDetails() {
        Model.instance.getProductById(productId, new Model.GetProductById() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(Product product) {
                title.setText(product.getTitle());
                String productPrice = product.getPrice();
                price.getEditText().setText(productPrice != null ? product.getPrice() : "");
                gender.setText(gender.getAdapter().getItem(Arrays.stream(genders).collect(Collectors.toList()).indexOf(product.getGender())).toString(), false);
                category.setText(category.getAdapter().getItem(Arrays.stream(categories).collect(Collectors.toList()).indexOf(product.getProductCategory())).toString(),false);
                condition.setText(condition.getAdapter().getItem(Arrays.stream(states).collect(Collectors.toList()).indexOf(product.getProductCondition())).toString(),false);
                description.setText(product.getDescription());
                if (product.getImageUrl() != null) {
                    Picasso.get().load(product.getImageUrl()).into(productImage);
                }
                isSold = product.isSold();
            }
        });
    }

    private void findAllElements(View view) {
        title = view.findViewById(R.id.productTitle);
        gender = view.findViewById(R.id.gender);
        category = view.findViewById(R.id.category);
        condition = view.findViewById(R.id.condition);
        description = view.findViewById(R.id.description);
        price = view.findViewById(R.id.productPrice);
        saveBtn = view.findViewById(R.id.addProductBtn);
        productImage = view.findViewById(R.id.productImage);
        camBtn = view.findViewById(R.id.cameraBtn);
        galleryBtn = view.findViewById(R.id.galleryBtn);
        progressBar = view.findViewById(R.id.add_product_progressbar);
        progressBar.setVisibility(View.GONE);
    }

    private void getStateDropdown(View view) {
        AutoCompleteTextView dropdown = view.findViewById(R.id.condition);
        this.states = new String[]{ProductCondition.GOOD_AS_NEW.toString(), ProductCondition.GOOD.toString(), ProductCondition.OK.toString()};
        setDropdownAdapter(dropdown, states);
    }

    private void getGenderDropdown(View view) {
        AutoCompleteTextView dropdown = view.findViewById(R.id.gender);
        this.genders = new String[]{Gender.FEMALE.toString(), Gender.MALE.toString(), Gender.OTHER.toString()};
        setDropdownAdapter(dropdown, genders);
    }

    private void getProductCategory(View view) {
        AutoCompleteTextView dropdown = view.findViewById(R.id.category);
        this.categories = new String[]{ProductCategory.BAKING.toString(), ProductCategory.COOKING.toString(), ProductCategory.DESERT.toString(),
        ProductCategory.OTHER.toString()};
        setDropdownAdapter(dropdown, categories);
    }

    private void setDropdownAdapter(AutoCompleteTextView dropdown, String[] items) {
        dropdown.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, items));
    }

    private void save() {
        saveBtn.setEnabled(false);
        camBtn.setEnabled(false);
        galleryBtn.setEnabled(false);

        Product product = getProduct(productId);
        saveProduct(product);
    }

    private void saveProduct(Product product) {
        progressBar.setVisibility(View.VISIBLE);

        if (imageBitmap == null) {
            Model.instance.saveProduct(product, () -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "saved product successfully!", Toast.LENGTH_LONG).show();
                Navigation.findNavController(this.title).navigateUp();
            });
        } else {
            Model.instance.saveProductImage(imageBitmap, UUID.randomUUID() + ".jpg", url -> {
                product.setImageUrl(url);
                Model.instance.saveProduct(product, () -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "saved product successfully!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(this.title).navigateUp();
                });
            });
        }
    }

    @NonNull
    private Product getProduct(String id) {
        final String key = id != null ? id : FirebaseDatabase.getInstance().getReference().push().getKey();
        return new Product(key,Objects.requireNonNull(this.title.getText()).toString(),
                this.description.getText() != null ? this.description.getText().toString() : "",
                this.isInArray(this.genders, this.gender.getText().toString()) ? this.gender.getText().toString() : Gender.OTHER.toString(),
                this.isInArray(this.states, this.condition.getText().toString()) ? this.condition.getText().toString() : ProductCondition.OK.toString(),
                this.isInArray(this.categories, this.category.getText().toString()) ? this.category.getText().toString() : ProductCategory.OTHER.toString(),
                Objects.requireNonNull(price.getEditText()).getText().toString(),
                Model.instance.mAuth.getUid(), currLocation != null ? currLocation.latitude : null, currLocation != null ? currLocation.longitude : null, false, this.isEditMode ? isSold : false);
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

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            try {
                this.mFusedLocationProviderClient = LocationServices
                        .getFusedLocationProviderClient(getActivity());

                this.mFusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                }).addOnSuccessListener(location -> {
                    currLocation = new LatLng(location.getLatitude(), location.getLongitude());
                });
            } catch (Error error) {
                Log.e("LocationError", error.getMessage());
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1337) {
            if (grantResults.length > 0) {
                getCurrentLocation();
            }
        }
    }
}