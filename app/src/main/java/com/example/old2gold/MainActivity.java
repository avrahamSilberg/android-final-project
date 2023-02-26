package com.example.old2gold;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.old2gold.fragments.UserProfileFragment;
import com.example.old2gold.model.MainViewModel;
import com.example.old2gold.model.Model;
import com.example.old2gold.model.User;
import com.example.old2gold.databinding.ActivityMainBinding;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private FirebaseAuth mAuth;
    private NavController navController;
    View headerView;
    User currentUser;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getData().observe(this, user -> {
            ImageView userImage = headerView.findViewById((R.id.imageView));
            if (user.getUserImageUrl() != null) {
                Picasso.get()
                        .load(user.getUserImageUrl())
                        .into(userImage);
            }

            TextView userName = headerView.findViewById(R.id.idUserName);
            userName.setText(user.getFirstName() + " " + user.getLastName());
        });


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_products, R.id.nav_favorites, R.id.nav_user_profile, R.id.nav_map_view)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        navigationView.getMenu().findItem(R.id.nav_sign_out).setOnMenuItemClickListener(menuItem -> {
            logout();
            return true;
        });

        this.mAuth = FirebaseAuth.getInstance();
        if (this.mAuth.getCurrentUser() != null) {
             headerView = navigationView.getHeaderView(0);

            Model.instance.getUser(mAuth.getCurrentUser().getUid(), user -> {
                TextView userName = headerView.findViewById(R.id.idUserName);
                userName.setText(user.getFirstName() + " " + user.getLastName());

                ImageView userImage = headerView.findViewById((R.id.imageView));
                if (user.getUserImageUrl() != null) {
                    Picasso.get()
                            .load(user.getUserImageUrl())
                            .into(userImage);
                }

                currentUser = user;


                ImageView imageView = headerView.findViewById(R.id.imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        navigateToUserProfile();
                    }
                });

                navigationView.getMenu().findItem(R.id.nav_user_profile).setOnMenuItemClickListener(menuItem -> {
                    navigateToUserProfile();
                    return true;
                });

            });

            TextView mail = headerView.findViewById(R.id.idMail);
            mail.setText(mAuth.getCurrentUser().getEmail());
        }
    }

    private void navigateToUserProfile() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", currentUser);

        UserProfileFragment userProfileFragment = new UserProfileFragment();
        userProfileFragment.setArguments(bundle);

        drawer.closeDrawer(Gravity.LEFT);
        navController.navigate(R.id.to_nav_user_profile, bundle);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}