package com.group.project.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.group.project.R;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.group.project.databinding.ItemContainerUserBinding;
import com.group.project.listeners.UserListeners;
import com.group.project.models.Users;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    //now insert the users in the list
    private final List<Users> users;
    private final UserListeners userListeners;
    private List<Users> selectedUsers;

    public UsersAdapter(List<Users> users, UserListeners userListeners) {
        this.users = users;
        this.userListeners = userListeners;
        selectedUsers = new ArrayList<>();
    }

    public List<Users> getSelectedUsers() {
        return selectedUsers;
    }

    //adapter view methods
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    //binding user data and layout and inserting them in the recycler view
    class  UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;
        ImageView imageAudioMeeting, imageVideoMeeting;
        ConstraintLayout userContainer;
        ImageView imageSelected;

        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
            //audio and video meeting
            imageAudioMeeting = itemView.findViewById(R.id.imageAudioMeeting);
            imageVideoMeeting = itemView.findViewById(R.id.imageVideoMeeting);
            userContainer = itemView.findViewById(R.id.userContainer);
            imageSelected = itemView.findViewById(R.id.imageSelected);
        }

        void setUserData(Users users) {
            binding.txtName.setText(users.username);
            binding.txtEmail.setText(users.email);
            binding.imageProfile.setImageBitmap(getUserImage(users.image));
            binding.imageProfile.setOnClickListener(v -> userListeners.onUserClicked(users));
            //Audio and video meeting
            binding.imageAudioMeeting.setOnClickListener(view -> userListeners.initiateAudioMeeting(users));
            binding.imageVideoMeeting.setOnClickListener(view -> userListeners.initiateVideoMeeting(users));

            userContainer.setOnLongClickListener(view -> {
                if (imageSelected.getVisibility() != View.VISIBLE) {
                    selectedUsers.add(users);
                    binding.imageSelected.setVisibility(View.VISIBLE);
                    binding.imageVideoMeeting.setVisibility(View.GONE);
                    binding.imageAudioMeeting.setVisibility(View.GONE);
                    userListeners.onMultipleUsersAction(true);
                }
                return true;
            });

            userContainer.setOnClickListener(view -> {
                if (imageSelected.getVisibility() == View.VISIBLE) {
                    selectedUsers.remove(users);
                    binding.imageSelected.setVisibility(View.GONE);
                    binding.imageVideoMeeting.setVisibility(View.VISIBLE);
                    binding.imageAudioMeeting.setVisibility(View.VISIBLE);
                    if (selectedUsers.size() == 0) {
                        userListeners.onMultipleUsersAction(false);
                    }
                } else {
                    if (selectedUsers.size() > 0) {
                        selectedUsers.add(users);
                        binding.imageSelected.setVisibility(View.VISIBLE);
                        binding.imageVideoMeeting.setVisibility(View.GONE);
                        binding.imageAudioMeeting.setVisibility(View.GONE);
                    }
                }
            });
        }

    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
