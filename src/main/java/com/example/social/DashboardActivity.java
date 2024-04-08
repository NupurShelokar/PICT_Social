package com.example.social;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.social.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
//    TextView mProfileTV;
    ActionBar actionBar;
    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar= getSupportActionBar();
        actionBar.setTitle("profile");

        firebaseAuth=FirebaseAuth.getInstance();
//        mProfileTV=findViewById(R.id.profileTv);
        BottomNavigationView navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        actionBar.setTitle("Home");
        HomeFragment fragment1=new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();

        checkUserStatus();
//        updateToken(FirebaseMessaging.getInstance().getToken());


    }


//    public void updateToken(String token){
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
//        Token mToken=new Token(token);
//        ref.child(mUID).setValue(mToken);
//    }

//    public void updateToken() {
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        String token = task.getResult();
//                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
//                        Token mToken = new Token(token);
//                        ref.child(mUID).setValue(mToken);
//                    } else {
//                        // Handle the error
//                        Log.e("TokenError", "Failed to get token: " + task.getException());
//                    }
//                });
//    }


    public void updateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
                        Token mToken = new Token(token);
                        if (mUID != null) {
                            ref.child(mUID).setValue(mToken)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("DashboardActivity", "Token updated successfully");
                                        } else {
                                            Log.e("DashboardActivity", "Failed to update token: " + task1.getException());
                                        }
                                    });
                        } else {
                            Log.e("DashboardActivity", "mUID is null");
                        }
                    } else {
                        // Handle the error
                        Log.e("TokenError", "Failed to get token: " + task.getException());
                    }
                });
    }



    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

//    public void updateToken() {
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            String token = task.getResult();
//                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
//                            Token mToken = new Token(token);
//                            ref.child(mUID).setValue(mToken);
//                        } else {
//                            // Handle error
//                        }
//                    }
//                });
//    }


    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        actionBar.setTitle("Home");
                        HomeFragment fragment1=new HomeFragment();
                        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                        ft1.replace(R.id.content,fragment1,"");
                        ft1.commit();
                        return true;
                    } else if (itemId == R.id.nav_profile) {
                        // Handle navigation to profile
                        actionBar.setTitle("Profile");
                        ProfileFragment fragment2=new ProfileFragment();
                        FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                        ft2.replace(R.id.content,fragment2,"");
                        ft2.commit();
                        return true;
                    } else if (itemId == R.id.nav_users) {
                        // Handle navigation to users
                        actionBar.setTitle("Users");
                        UsersFragment fragment3=new UsersFragment();
                        FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                        ft3.replace(R.id.content,fragment3,"");
                        ft3.commit();
                        return true;
                    }else if (itemId == R.id.nav_chat) {
                        // Handle navigation to users
                        actionBar.setTitle("Chats");
                        ChatListFragment fragment4=new ChatListFragment();
                        FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                        ft4.replace(R.id.content,fragment4,"");
                        ft4.commit();
                        return true;
                    }

                    return false;
                }
            };

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null) {
//            mProfileTV.setText(user.getEmail());
            mUID=user.getUid();

            SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();

            updateToken();
        }
        else{
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }


}