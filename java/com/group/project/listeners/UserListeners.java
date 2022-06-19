package com.group.project.listeners;


import com.group.project.models.Users;

public interface UserListeners {
    void onUserClicked(Users users);

    void initiateVideoMeeting(Users users);

    void initiateAudioMeeting(Users users);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);
}
