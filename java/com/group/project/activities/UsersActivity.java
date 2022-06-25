package com.group.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.group.project.adapters.UsersAdapter;
import com.group.project.databinding.ActivityUsersBinding;
import com.group.project.listeners.UserListeners;
import com.group.project.models.Users;
import com.group.project.utils.Constants;
import com.group.project.utils.PreferenceManager;
import com.group.project.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsersActivity extends BaseActivity implements UserListeners {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private ImageView imageConference;
    private UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        imageConference = findViewById(R.id.imageConference);

        setListeners();
        getUsers();
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
    //back button
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    //error message
    private void showErrorMessage() {
        binding.txtErrorMessage.setText(String.format("%s", "No Counsellor available"));
        binding.txtErrorMessage.setVisibility(View.GONE);
    }

    //get users from firestore
    private void getUsers () {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null ) {
                        List<Users> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            Users user = new Users();
                            user.username = queryDocumentSnapshot.getString(Constants.KEY_USERNAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        //connecting users to recycler view
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerview.setAdapter(usersAdapter);
                            binding.usersRecyclerview.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    //when user is clicked
    @Override
    public void onUserClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), ChattingPageActivity.class);
        intent.putExtra(Constants.KEY_USER, users);
        startActivity(intent);
        finish();
    }

    //progress bar
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //Audio and video meeting

    @Override
    public void initiateVideoMeeting(Users users) {
        if (users.token == null || users.token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    users.username + " is not available for Video Call",
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
            intent.putExtra(Constants.KEY_USER, users);
            //intent.putExtra("user", users);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(Users users) {
        if (users.token == null || users.token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    users.username + " is not available for Audio Call",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
            intent.putExtra("user", users);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected) {
            imageConference.setVisibility(View.VISIBLE);
            imageConference.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
                intent.putExtra("selectedUsers", new Gson().toJson(usersAdapter.getSelectedUsers()));
                intent.putExtra("type", "video");
                intent.putExtra("isMultiple", true);
                startActivity(intent);
            });
        } else {
            imageConference.setVisibility(View.GONE);
        }
    }
}