package com.example.old2gold.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.old2gold.R;
import com.example.old2gold.model.FavoriteProductListRvViewModel;
import com.example.old2gold.model.Model;
import com.example.old2gold.model.Product;
import com.example.old2gold.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductDetailsFragment extends Fragment {

    ImageView imageUrl, sellerImage;
    TextView title,  category, description, sellerName;
    FloatingActionButton editButton, deleteButton;
    ProgressBar progressBar;
    View view;
    ImageView removeFromFavorites;
    ImageView addToFavorite;
    FavoriteProductListRvViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(FavoriteProductListRvViewModel.class);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_product_details, container, false);
        String productId = ProductDetailsFragmentArgs.fromBundle(getArguments()).getProductId();

        attachFragmentElement(view);

        Model.instance.getProductById(productId, new Model.GetProductById() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(Product product) {
                setFragmentElements(product);
                displayOwnerButtons(product, productId);
                getProductSeller(product);
                FavoritesHandler(product, productId);
            }
        });


        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void FavoritesHandler(Product product, String productId) {
        viewModel.getData().observe(getViewLifecycleOwner(), list1 -> showFavoritesIcon(product));
        if (!isCurrentUserProduct(product.getContactId())) {
            addToFavorite = view.findViewById(R.id.add_to_favorite);
            removeFromFavorites = view.findViewById(R.id.in_favorite_icon);
            HandleRemoveFromFavorites(productId);
            HandleAddToFavorites(productId);
        }
    }

    private void HandleRemoveFromFavorites(String productId) {
        removeFromFavorites.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (removeFromFavorites.getVisibility() == View.VISIBLE) {
                    removeFromFavorites.setVisibility(View.GONE);
                    addToFavorite.setVisibility(View.VISIBLE);
                }
                Model.instance.removeFromLikedProducts(productId, () -> {
                });
            }
        });
    }

    private void HandleAddToFavorites(String productId) {
        addToFavorite.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (addToFavorite.getVisibility() == View.VISIBLE) {
                    removeFromFavorites.setVisibility(View.VISIBLE);
                    addToFavorite.setVisibility(View.GONE);
                }

                Model.instance.addToLikedProducts(productId, () -> {
                });
            }
        });
    }

    private boolean isCurrentUserProduct(String contactId) {
        return Model.instance.mAuth.getUid().equals(contactId);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getProductSeller(Product product) {
        Model.instance.getProductSellerUser(product.getContactId(), user -> {
            progressBar.setVisibility(View.GONE);

            if (user != null) {
                if (user.getUserImageUrl() != null) {
                    Picasso.get()
                            .load(user.getUserImageUrl())
                            .into(sellerImage);
                } else {
                    sellerImage.setImageResource(R.drawable.no_person_image);
                }

                sellerName.setText(user.getFirstName() + " " + user.getLastName());
                sellerName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateToUserProfile(user, product.getContactId());
                    }
                });
                sellerImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateToUserProfile(user, product.getContactId());
                    }
                });
            } else {
                sellerImage.setVisibility(View.GONE);
                sellerName.setText("seller not found ):");
            }
        });
    }

    private void displayOwnerButtons(Product product, String productId) {
        if (isCurrentUserProduct(product.getContactId())) {
            displayEditButton(productId);
            displayDeleteButton(product);
            removeFromFavorites.setVisibility(View.GONE);
            addToFavorite.setVisibility(View.GONE);
        }
    }

    private void displayDeleteButton(Product product) {
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteDialog(product);
            }
        });
    }


    private void displayEditButton(String productId) {
        editButton.setVisibility(View.VISIBLE);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(ProductDetailsFragmentDirections.navProductDetailsToNavAddProduct(productId));
            }
        });
    }

    private void setFragmentElements(Product product) {
        title.setText(product.getTitle());
        category.setText(product.getProductCategory());
        description.setText(product.getDescription());
        if (product.getImageUrl() != null) {
            Picasso.get().load(product.getImageUrl()).into(imageUrl);
        }
    }

    private void attachFragmentElement(View view) {
        imageUrl = view.findViewById(R.id.detailsProductImage);
        title = view.findViewById(R.id.detailsProductTitle);
//        gender = view.findViewById(R.id.detailProductGender);
        category = view.findViewById(R.id.detailProductCategory);
//        condition = view.findViewById(R.id.detailsProductCondition);
        description = view.findViewById(R.id.detailsProductDescription);
        editButton = view.findViewById(R.id.floatingEditButton);
        deleteButton = view.findViewById(R.id.floatingDeleteButton);
        sellerImage = view.findViewById(R.id.seller_image);
        sellerName = view.findViewById(R.id.seller_name);
        progressBar = view.findViewById(R.id.products_details_progress_bar);
        addToFavorite = view.findViewById(R.id.add_to_favorite);
        removeFromFavorites = view.findViewById(R.id.in_favorite_icon);

    }

    private void openDeleteDialog(Product product) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Do you want to delete this product?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                product.setDeleted(true);
                Model.instance.saveProduct(product, () -> {
                    Navigation.findNavController(title).navigateUp();
                });
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();
    }

    private void navigateToUserProfile(User user, String contactId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);

        UserProfileFragment userProfileFragment = new UserProfileFragment();
        userProfileFragment.setArguments(bundle);

        if (Model.instance.mAuth.getUid().equals(contactId)) {
            Navigation.findNavController(view).navigate(R.id.to_nav_user_profile, bundle);
        } else {
            Navigation.findNavController(view).navigate(R.id.to_other_user_profile, bundle);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showFavoritesIcon(Product product) {
        if (!isCurrentUserProduct(product.getContactId())) {
            List<String> favoritesProducts = viewModel.getData().getValue()
                    .stream()
                    .map(Product::getId).collect(Collectors.toList());

            if (favoritesProducts.contains(product.getId())) {
                removeFromFavorites.setVisibility(View.VISIBLE);
                addToFavorite.setVisibility(View.GONE);
            } else {
                addToFavorite.setVisibility(View.VISIBLE);
                removeFromFavorites.setVisibility(View.GONE);
            }
        }
    }

}