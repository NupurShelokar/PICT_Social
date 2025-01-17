package com.example.social;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText mEmailEt,mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTV;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Create Account");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mEmailEt=findViewById(R.id.emailEt);
        mPasswordEt=findViewById(R.id.passwordEt);
        mRegisterBtn=findViewById(R.id.registerBtn);
        mHaveAccountTV=findViewById(R.id.have_accountTV);

        mAuth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmailEt.getText().toString().trim();
                String password=mPasswordEt.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if(password.length()<6){
                    mPasswordEt.setError("Password length at least 6 character");
                    mEmailEt.setFocusable(true);
                }
                else{
                    registerUser(email,password);
                }
            }
        });

        mHaveAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });


    }

    private void registerUser(String email, String password) {

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            FirebaseUser user=mAuth.getCurrentUser();

                            String email=user.getEmail();
                            String uid=user.getUid();
                            HashMap<Object,String>hashmap=new HashMap<>();
                            hashmap.put("email",email);
                            hashmap.put("uid",uid);
                            hashmap.put("onlineStatus","online");
                            hashmap.put("typingTo","noOne");
                            hashmap.put("name","");
                            hashmap.put("phone","");
                            hashmap.put("image","");
                            hashmap.put("cover","");
                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            DatabaseReference reference=database.getReference("Users");
                            reference.child(uid).setValue(hashmap);

                            Toast.makeText(RegisterActivity.this,"Registered...\n"+user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();

                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this,"Authentication Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}