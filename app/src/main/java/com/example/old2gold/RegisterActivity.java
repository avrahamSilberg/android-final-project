package com.example.old2gold;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.old2gold.model.Model;
import com.example.old2gold.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.UUID;

import lombok.SneakyThrows;

public class RegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private TextInputEditText firstNameEdt, lastNameEdt, passwordEdt, confirmPwdEdt,
            emailEdt, phoneNumberEdt;
    private ImageView productImage;
    private FloatingActionButton camBtn, galleryBtn;
    private TextView loginTV;
    private Button registerBtn;
    private FirebaseAuth mAuth;
    private Bitmap imageBitmap;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameEdt = findViewById(R.id.idEdtFirstName);
        lastNameEdt = findViewById(R.id.idEdtLastName);
        passwordEdt = findViewById(R.id.idEdtPassword);
        confirmPwdEdt = findViewById(R.id.idEdtConfirmPassword);
        emailEdt = findViewById(R.id.idEdtEmail);
        phoneNumberEdt = findViewById(R.id.idEdtPhoneNumber);

        loginTV = findViewById(R.id.idTVLoginUser);
        registerBtn = findViewById(R.id.idBtnRegister);
        mAuth = FirebaseAuth.getInstance();
        productImage = findViewById(R.id.productImage);

        camBtn = findViewById(R.id.cameraBtn);
        camBtn.setOnClickListener(v -> openCam());

        galleryBtn = findViewById(R.id.galleryBtn);
        galleryBtn.setOnClickListener(v -> openGallery());
        progressBar = findViewById(R.id.register_progressBar);
        progressBar.setVisibility(View.GONE);

        // adding on click for login tv.
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // opening a login activity on clicking login text.
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        // adding click listener for register button.
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.GONE);

                String firstName = firstNameEdt.getText().toString();
                String lastName = lastNameEdt.getText().toString();
                String email = emailEdt.getText().toString();
                String phoneNumber = phoneNumberEdt.getText().toString();

                String pwd = passwordEdt.getText().toString();
                String cnfPwd = confirmPwdEdt.getText().toString();

                if (!pwd.equals(cnfPwd) && !TextUtils.isEmpty(pwd)) {
                    Toast.makeText(RegisterActivity.this, "Please check both having same password..", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(cnfPwd) || TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(RegisterActivity.this, "Please make sure all fields are filled", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    // on below line we are creating a new user by passing email and password.
                    mAuth.createUserWithEmailAndPassword(email, pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // on below line we are checking if the task is success or not.
                                    if (task.isSuccessful()) {
                                        User user = new User(firstName, lastName, email, phoneNumber,"", new ArrayList<>());
                                        if (imageBitmap != null) {
                                            Model.instance.saveUserImage(imageBitmap, UUID.randomUUID() + ".jpg", url -> {
                                                user.setUserImageUrl(url);
                                                Model.instance.saveUser(user, task.getResult().getUser().getUid());
                                            });
                                        } else {
                                            Model.instance.saveUser(user, task.getResult().getUser().getUid());
                                        }

                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegisterActivity.this, "User Registered..", Toast.LENGTH_SHORT).show();

                                        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(i);
                                        finish();
                                    } else {

                                        // in else condition we are displaying a failure toast message.
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegisterActivity.this, "Fail to register user..", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }


    private void openCam() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
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
                if (selectedImageUri != null) {
                    productImage.setImageURI(selectedImageUri);
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                }
            }
        }
    }

}