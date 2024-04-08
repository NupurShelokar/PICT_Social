package com.example.social;

import android.content.Intent;
import android.os.Bundle;

import com.example.social.adapters.AdapterChat;
import com.example.social.models.ModelChat;
import com.example.social.models.ModelUser;
import com.example.social.notifications.APIService;
import com.example.social.notifications.Client;
import com.example.social.notifications.Data;
import com.example.social.notifications.Response;
import com.example.social.notifications.Sender;
import com.example.social.notifications.Token;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat>chatList;
    AdapterChat adapterChat;
    String hisUid;
    String myUid;
    String hisImage;

    APIService apiService;
    boolean notify=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.chat_recyclerView);
        profileIv=findViewById(R.id.profileIv);
        nameTv=findViewById(R.id.nameTv);
        messageEt = findViewById(R.id.messageEt);
        userStatusTv=findViewById(R.id.userStatusTv);
        sendBtn=findViewById(R.id.sendBtn);
        firebaseAuth=FirebaseAuth.getInstance();

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
//        checkUserStatus();

        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid");

        firebaseDatabase=FirebaseDatabase.getInstance();
        userDbRef=firebaseDatabase.getReference("Users");

        Query userQuery=userDbRef.orderByChild("uid").equalTo(hisUid);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    hisImage=""+ds.child("image").getValue();
                    String typingStatus=""+ds.child("typingTo").getValue();
                    String onlineStatus =""+ds.child("onlineStatus").getValue();

                    if(typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");
                    }
                    else{
                        if(onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }
                        else{
                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dataTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            userStatusTv.setText("Last seen at: "+dataTime);
                        }
                    }


                    nameTv.setText(name);
                    try{
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_white).into(profileIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_white).into(profileIv);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
        sendBtn.setOnClickListener(v -> {
            notify=true;
            String message=messageEt.getText().toString().trim();
            if(TextUtils.isEmpty(message)){
                Toast.makeText(ChatActivity.this,"Cannot send the empty message..",Toast.LENGTH_SHORT).show();
            }
            else{
                sendMessage(message);
            }
            messageEt.setText("");
        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }
                else{
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessage();
        seenMessage();
    }

//    private void seenMessage() {
//        Log.d("ChatActivity", "readMessage() method called");
//
//        userRefForSeen=FirebaseDatabase.getInstance().getReference("Chats");
//        seenListener=userRefForSeen.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for(DataSnapshot ds:snapshot.getChildren()){
//                    ModelChat chat= ds.getValue(ModelChat.class);
//                    if(chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)){
//                        HashMap<String, Object>hasSeenHashMap=new HashMap<>();
//                        hasSeenHashMap.put("isSeen",true);
//                        ds.getRef().updateChildren(hasSeenHashMap);
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void seenMessage() {
        Log.d("ChatActivity", "seenMessage() method called");

        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat != null && chat.getReceiver() != null && chat.getSender() != null &&
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



//    private void readMessage() {
//        Log.d("ChatActivity", "seenMessage() method called");
//
//        chatList=new ArrayList<>();
//        DatabaseReference dbRef=firebaseDatabase.getInstance().getReference("Chats");
//        dbRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                chatList.clear();
//                for(DataSnapshot ds: snapshot.getChildren()){
//                    ModelChat chat=ds.getValue(ModelChat.class);
//                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
//                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)
//                    ) {
//
//                        chatList.add(chat);
//                    }
//
//                    adapterChat=new AdapterChat(ChatActivity.this,chatList,hisImage);
//                    adapterChat.notifyDataSetChanged();
//
//                    recyclerView.setAdapter(adapterChat);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void readMessage() {
        Log.d("ChatActivity", "readMessage() method called");

        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat != null) { // Add null check here
                        if ((chat.getReceiver() != null && chat.getReceiver().equals(myUid) && chat.getSender() != null && chat.getSender().equals(hisUid)) ||
                                (chat.getReceiver() != null && chat.getReceiver().equals(hisUid) && chat.getSender() != null && chat.getSender().equals(myUid))) {
                            chatList.add(chat);
                        }
                    }
                }
                // Initialize adapter only once, outside the loop
                if (adapterChat == null) {
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    recyclerView.setAdapter(adapterChat);
                } else {
                    // Notify adapter if it's already initialized
                    adapterChat.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatActivity", "readMessage() onCancelled: " + error.getMessage());
            }
        });
    }







    private void sendMessage(String message) {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        String timeStamp=String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("isSeen",false );
        databaseReference.child("Chats").push().setValue(hashMap);


        final DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user=snapshot.getValue(ModelUser.class);
                if(notify){
                    sendNotification(hisUid,user.getName(),message);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String hisUid, String name, String message) {
        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren() ){
                    Token token=ds.getValue(Token.class);
                    Data data=new Data(myUid,name+":"+message,"New Message",hisUid,R.drawable.ic_default_img);

                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this,""+response.message(),Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null) {
//            mProfileTV.setText(user.getEmail());
            myUid=user.getUid();
        }
        else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);
        dbRef.updateChildren(hashMap);
    }


    private void checkTypingStatus(String typing){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("typingTo",typing);
        dbRef.updateChildren(hashMap);
    }


    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }


    @Override
    protected void onPause() {
        super.onPause();
        String timestamp=String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}