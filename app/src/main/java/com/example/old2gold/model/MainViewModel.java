package com.example.old2gold.model;


import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainViewModel extends ViewModel {
    LiveData<User> data;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MainViewModel() {
        data = Model.instance.getLoggedUser();
    }

    public LiveData<User> getData() {
        return data;
    }
}