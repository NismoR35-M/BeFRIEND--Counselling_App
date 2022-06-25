package com.group.project.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.group.project.adapters.RecentConversationsAdapter;
import com.group.project.databinding.ActivityMainBinding;
import com.group.project.listeners.ConversionListener;
import com.group.project.models.ChatMessage;
import com.group.project.models.Users;
import com.group.project.utils.Constants;
import com.group.project.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    //disable Battery optimization
    private int REQUEST_CODE_BATTERY_OPTIMIZATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());


        init();
        loadUserDetails();
       // getToken();
        setListeners();
        listenConversations();
        //checkForBatteryOptimizations();

        getSupportActionBar().hide();


    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationRecyclerview.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    //binding sign out with its button
    private void setListeners() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    //load user details
    private void loadUserDetails() {
        binding.txtName.setText(preferenceManager.getString(Constants.KEY_USERNAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    //Toast message
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations () {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_USERNAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_USERNAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_USER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecyclerview.smoothScrollToPosition(0);
            binding.conversationRecyclerview.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    //get token
    private void getToken() {

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateUserToken);
    }

    //Update user token
    private void updateUserToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    //sign out
    private void signOut() {
        showToast("Signing out....");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));

    }

    @Override
    public void onConversionClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), ChattingPageActivity.class);
        intent.putExtra(Constants.KEY_USER, users);
        startActivity(intent);
    }

//    private void checkForBatteryOptimizations() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("warning");
//                builder.setMessage("Battery optimization is enabled. It can interrupt running background services");
//                builder.setPositiveButton("Disable", (dialogInterface, i) -> {
//                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
//                });
//                builder.setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss());
//                builder.create().show();
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION) {
//            checkForBatteryOptimizations();
//        }
//    }
}