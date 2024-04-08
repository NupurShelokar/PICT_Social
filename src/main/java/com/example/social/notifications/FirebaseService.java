package com.example.social.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

//public class FirebaseService extends FirebaseMessagingService {
//
//    @Override
//    public void onNewToken(@NonNull String token) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        if(user!=null){
//            updateToken(token);
//        }
//    }
//
//    private void updateToken(String token) {
//        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
//        Token tokenRefresh=new Token(token);
//        ref.child(user.getUid()).setValue(tokenRefresh);
//    }
//}

public class FirebaseService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            updateToken(token, user.getUid());
        }
    }

    private void updateToken(String token, String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token tokenRefresh = new Token(token);
        ref.child(userId).setValue(tokenRefresh);
    }
}

