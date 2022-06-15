package com.group.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.group.project.R;

import com.google.firebase.auth.FirebaseAuth;
import com.group.project.adapters.FragmentAdapter;
import com.group.project.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //for logout
        mAuth = FirebaseAuth.getInstance();

        //set up viewpager to hold fragments
        binding.viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }
    //setting menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                //Toast.makeText(this, "Settings is clicked.", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(MainActivity.this, ArticlesActivity.class);
                startActivity(intent2);
                break;
            case R.id.groupChat:
                //Toast.makeText(this, "Groupchat clicked", Toast.LENGTH_SHORT).show();
                /*Go to groupchat*/
                Intent i = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(i);
                break;
            case R.id.logout:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}