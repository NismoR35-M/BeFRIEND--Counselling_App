package com.group.project.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.group.project.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.group.project.activities.ChattingPageActivity;
import com.group.project.activities.IncomingInvitationActivity;
import com.group.project.databinding.ActivityIncomingInvitationBinding;
import com.group.project.models.Users;
import com.group.project.utils.Constants;

import java.util.Random;

import timber.log.Timber;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Timber.d("Token:%s", token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Users users = new Users();
        users.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        users.username = remoteMessage.getData().get(Constants.KEY_USERNAME);
        users.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, ChattingPageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, users);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notifications);
        builder.setContentTitle(users.username);
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(Constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());

        //Incoming call
        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);
        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                Intent i = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                i.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                i.putExtra(
                        Constants.KEY_USERNAME,
                        remoteMessage.getData().get(Constants.KEY_USERNAME)
                );
                i.putExtra(
                        Constants.KEY_EMAIL,
                        remoteMessage.getData().get(Constants.KEY_EMAIL)
                );
                i.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                i.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                Intent intent1 = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent1.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
            }
        }
    }
}
