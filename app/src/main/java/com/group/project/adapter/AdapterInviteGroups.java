package com.group.project.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.group.project.R;
import com.group.project.group.GroupChatActivity;
import com.group.project.model.ModelGroups;
import com.group.project.model.UserModel;
import com.group.project.notification.Data;
import com.group.project.notification.Sender;
import com.group.project.notification.Token;
import com.group.project.party.InviteActivity;
import com.group.project.party.InviteMoreActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterInviteGroups extends RecyclerView.Adapter<AdapterInviteGroups.MyHolder>{

    final Context context;
    final List<ModelGroups> userList;
    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterInviteGroups(Context context, List<ModelGroups> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.chat_list_group, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        ModelGroups modelChatListGroups = userList.get(position);

        FirebaseFirestore.getInstance().collection("groups").document(modelChatListGroups.getGroup()).addSnapshotListener((value, error) -> {

            holder.name.setText(Objects.requireNonNull(value.get("name")).toString());

            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(holder.dp);
            }

        });

        holder.itemView.setOnClickListener(view -> {

            String stamp;
            if (InviteMoreActivity.getRoom().isEmpty()){
                stamp = InviteActivity.getRoom();
            }else {
                stamp = InviteMoreActivity.getRoom();
            }
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            if (InviteActivity.getRoom().isEmpty()){
                hashMap.put("msg", InviteMoreActivity.getRoom());
            }else {
                hashMap.put("msg", InviteActivity.getRoom());
            }
            hashMap.put("type", "party");
            hashMap.put("timestamp", stamp);
            FirebaseDatabase.getInstance().getReference("Groups").child(modelChatListGroups.getId()).child("Message").child(stamp)
                    .setValue(hashMap);
            notify = true;
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                if (notify){
                    FirebaseDatabase.getInstance().getReference("Groups").child(modelChatListGroups.getTimestamp()).child("Participants").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent watch party invitation");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                notify = false;

            });

        });

        holder.itemView.setOnLongClickListener(view -> {

            Intent intent = new Intent(context, GroupChatActivity.class);
            intent.putExtra("group", modelChatListGroups.getGroup());
            context.startActivity(intent);

            return false;
        });
    }

    private void sendNotification(final String hisId, final String name,final String message){
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " : " + message, "New Message", hisId, "chat",R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        //noinspection deprecation
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key="+context.getResources().getString(R.string.server_key));
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @SuppressWarnings("unused")
    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView username;
        final TextView time;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.time);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.phone);




            MobileAds.initialize(itemView.getContext(), initializationStatus -> {
            });
//            AdLoader.Builder builder = new AdLoader.Builder(itemView.getContext(), itemView.getContext().getString(R.string.native_ad_unit_id));
//            builder.forUnifiedNativeAd(unifiedNativeAd -> {
//                TemplateView templateView = itemView.findViewById(R.id.my_template);
//                templateView.setNativeAd(unifiedNativeAd);
//            });
//
//            AdLoader adLoader = builder.build();
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adLoader.loadAd(adRequest);

        }

    }
}
