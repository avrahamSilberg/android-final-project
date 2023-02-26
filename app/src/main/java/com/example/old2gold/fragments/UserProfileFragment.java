package com.example.old2gold.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.old2gold.R;
import com.example.old2gold.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment {
    TextView email, phone, address, fullUserName,productsTitle, favoritesTitle;
    ImageButton userFavorites, userProducts, editDetails;
    ImageView userImage;
    User currentUser;
    FirebaseAuth mAuth;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        fullUserName = view.findViewById(R.id.full_user_name);
        email = view.findViewById(R.id.email_txt);
        phone = view.findViewById(R.id.phone_txt);
        address = view.findViewById(R.id.address_txt);
        userFavorites = view.findViewById(R.id.user_favorites);
        userProducts = view.findViewById(R.id.user_products);
        userImage = view.findViewById(R.id.user_image);
        editDetails = view.findViewById(R.id.edit_details);
        productsTitle = view.findViewById(R.id.products_title);
        favoritesTitle = view.findViewById(R.id.favorites_title);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            currentUser = (User) bundle.getSerializable("user");
        }
        email.setText(currentUser.getEmail());
        phone.setText(currentUser.getPhoneNumber());
        address.setText(currentUser.getAddress());
        fullUserName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());


        if (currentUser.getUserImageUrl() != null) {
            Picasso.get()
                    .load(currentUser.getUserImageUrl())
                    .into(userImage);
        }

        mAuth = FirebaseAuth.getInstance();


        // show edit button only if the user is logged user
        int visibility = currentUser.getId().equals(mAuth.getCurrentUser().getUid()) ? View.VISIBLE : View.INVISIBLE;
        editDetails.setVisibility(visibility);
        userFavorites.setVisibility(visibility);
        userProducts.setVisibility(visibility);
        productsTitle.setVisibility(visibility);
        favoritesTitle.setVisibility(visibility);

        userFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.to_nav_favorites);
            }
        });

        userProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.to_nav_my_products);
            }
        });

        editDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", currentUser);

                EditUserDetailsFragment editDetailsFragment = new EditUserDetailsFragment();
                editDetailsFragment.setArguments(bundle);

                Navigation.findNavController(view).navigate(R.id.to_edit_user_details, bundle);
            }
        });

        return view;
    }
}