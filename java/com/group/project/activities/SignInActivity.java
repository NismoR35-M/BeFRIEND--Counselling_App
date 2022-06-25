package com.group.project.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group.project.databinding.ActivitySignInBinding;
import com.group.project.utils.Constants;
import com.group.project.utils.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    ProgressDialog progressDialog;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //hide toolbar
        getSupportActionBar().hide();

        //firebase DB and authentication instances
        setListeners();

    }

    private void setListeners() {
        binding.txtRegister.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.txtLogin.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
    }

    private void signIn() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection((Constants.KEY_COLLECTION_USERS))
                .whereEqualTo(Constants.KEY_EMAIL, binding.txtEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.txtPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.txtLogin.setVisibility(View.INVISIBLE);
            binding.progrssBar.setVisibility(View.VISIBLE);
        } else {
            binding.progrssBar.setVisibility(View.INVISIBLE);
            binding.txtLogin.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isValidSignInDetails() {
        if (binding.txtEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.txtEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.txtPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }


//    //put data on firestore
//    private void addDataOnFirestore() {
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("Email", "nismo@gmail.com");
//        database.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "Data inserted", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(exception -> {
//                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

}