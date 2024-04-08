package com.example.social;


import static android.app.Activity.RESULT_OK;

import android.Manifest;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.social.adapters.AdapterPost;
import com.example.social.models.ModelPost;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String storagePath="Users_Profile_Cover_Imgs/";

    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv;
    FloatingActionButton fab;

    ProgressDialog pd;
    RecyclerView postRecyclerView;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;
    private static final int IMAGE_PICK_COVER_CODE=500;
    private static final int IMAGE_PICK_PROFILE_CODE=600;

    int imgCode;

    String cameraPermission[];
    String storagePermission[];

    List<ModelPost>postList;
    AdapterPost adapterPost;
    String uid;

    Uri image_uri;

    String profileOrCoverPhoto;
    public ProfileFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile,container,false);
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        avatarIv=view.findViewById(R.id.avatarIv);
        coverIv=view.findViewById(R.id.coverIv);
        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);
        fab=view.findViewById(R.id.fab);
        postRecyclerView=view.findViewById(R.id.recyclerview_posts);

        pd=new ProgressDialog(getActivity());

        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try{
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }

                    try{
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList=new ArrayList<>();

        checkUserStatus();
        loadMyPosts();

        return view;
    }

//    private void loadMyPosts() {
//
//        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
//        layoutManager.setStackFromEnd(true);
//        layoutManager.setReverseLayout(true);
//        postRecyclerView.setLayoutManager(layoutManager);
//
//        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
//        Query query=ref.orderByChild("uid").equalTo(uid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                postList.clear();
//                for(DataSnapshot ds:snapshot.getChildren()){
//                    ModelPost myPosts=ds.getValue(ModelPost.class);
//                    postList.add(myPosts);
//                    adapterPost=new AdapterPost(getActivity(),postList);
//                    postRecyclerView.setAdapter(adapterPost);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void searchMyPosts(String searchQuery) {
//
//        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
//        layoutManager.setStackFromEnd(true);
//        layoutManager.setReverseLayout(true);
//        postRecyclerView.setLayoutManager(layoutManager);
//
//        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
//        Query query=ref.orderByChild("uid").equalTo(uid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                postList.clear();
//                for(DataSnapshot ds:snapshot.getChildren()){
//                    ModelPost myPosts=ds.getValue(ModelPost.class);
//
//                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
//                    myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
//                        postList.add(myPosts);
//                    }
//
//                    adapterPost=new AdapterPost(getActivity(),postList);
//                    postRecyclerView.setAdapter(adapterPost);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);
                    postList.add(myPosts);
                }
                // Initialize adapter once outside the loop
                adapterPost = new AdapterPost(getActivity(), postList);
                postRecyclerView.setAdapter(adapterPost);
                // Notify adapter after adding all posts
                adapterPost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMyPosts(String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(myPosts);
                    }
                }
                // Initialize adapter once outside the loop
                adapterPost = new AdapterPost(getActivity(), postList);
                postRecyclerView.setAdapter(adapterPost);
                // Notify adapter after adding all posts
                adapterPost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED); ///denied
        return  result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermission,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermission,CAMERA_REQUEST_CODE);
    }


    private void showEditProfileDialog(){
        String options[]={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto="image";
                    showImagePicDialog();
                }
                else if(which==1){
                    pd.setMessage("Updating cover photo");
                    profileOrCoverPhoto="cover";
                    showImagePicDialog();
                }
                else if(which==2){
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");
                }
                else if(which==3){
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("update"+key);
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        EditText editText=new EditText(getActivity());
        editText.setHint("Enter"+key);
        linearLayout.addView(editText);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editText.setLayoutParams(layoutParams);
//        linearLayout.addView(editText);
        builder.setView(linearLayout);

        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value=editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object>result=new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                }
                            });

                    if(key.equals("name")){
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                        Query query=ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    String child=ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                else{
                    Toast.makeText(getActivity(),"Please enter"+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    //        String options[]={"Camera", "Gallery"};
//        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
//        builder.setTitle("Pick Image From");
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if(which==0){
//                    //camera
//                    if(!checkCameraPermission()){
//                        requestCameraPermission();
//                    }
//                    else{
//                        pickFromCamera();
//                    }
//                }
//                else if(which==1){
//                    //gallery
//                    if(!checkStoragePermission()){
//                        requestStoragePermission();
//                    }
//                    else{
//                        pickFromGallery();
//                    }
//                }
//            }
//        });
//
//        builder.create().show();




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) { // Check if grantResults array has at least 2 elements
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle unexpected case where grantResults array doesn't have enough elements
                    Toast.makeText(getActivity(), "Unexpected result from permission request", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length >0) { // Check if grantResults array has at least 2 elements

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle unexpected case where grantResults array doesn't have enough elements
                    Toast.makeText(getActivity(), "Unexpected result from permission request", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode,resultCode,data);
        Uri uri=data.getData();

        if ("image".equals(profileOrCoverPhoto)) {
            // Update profile image view
            avatarIv.setImageURI(uri);
            if (uri != null) {
                uploadProfileCoverPhoto(uri);
            } else {
                Toast.makeText(getActivity(), "Failed to get image URI", Toast.LENGTH_SHORT).show();
            }
        }
        if ("cover".equals(profileOrCoverPhoto)) {
            // Update cover image view
            coverIv.setImageURI(uri);
            if (uri != null) {
                uploadProfileCoverPhoto(uri);
            } else {
                Toast.makeText(getActivity(), "Failed to get image URI", Toast.LENGTH_SHORT).show();
            }
        }

    }

    //        if(resultCode==RESULT_OK){
//            if(requestCode==IMAGE_PICK_GALLERY_CODE){
//                image_uri=data.getData();
//                uploadProfileCoverPhoto(image_uri);
//            }
//            if(requestCode==IMAGE_PICK_CAMERA_CODE){
//                uploadProfileCoverPhoto(image_uri);
//            }
//
//            super.onActivityResult(requestCode,resultCode,data);
//        }



//    private void uploadProfileCoverPhoto(Uri uri) {
//        pd.show();
//
//        String filePathAndName= storagePath+""+profileOrCoverPhoto+"_"+user.getUid();
//        StorageReference storageReference2nd=storageReference.child(filePathAndName);
//        storageReference2nd.putFile(uri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
//                        while(!uriTask.isSuccessful());
//                        Uri downloadUri=uriTask.getResult();
//
//                        if(uriTask.isSuccessful()){
//                            HashMap<String,Object>results=new HashMap<>();
//                            results.put(profileOrCoverPhoto,downloadUri.toString());
//
//                            databaseReference.child(user.getUid()).updateChildren(results)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//                                            pd.dismiss();
//                                            Toast.makeText(getActivity(),"Image Updated",Toast.LENGTH_SHORT).show();
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            pd.dismiss();
//                                            Toast.makeText(getActivity(),"Error Updating Image",Toast.LENGTH_SHORT).show();
//
//                                        }
//                                    });
//                        }
//                        else{
//                            pd.dismiss();
//                            Toast.makeText(getActivity(),"some error occured",Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        pd.dismiss();
//                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();

        String filePathAndName = storagePath + profileOrCoverPhoto + "_" + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);

        storageReference2nd.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    Task<Uri> downloadUriTask = taskSnapshot.getStorage().getDownloadUrl();
                    downloadUriTask.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            // Update the database with the download URL
                            updateProfileCoverPhoto(downloadUri);
                        } else {
                            // Handle failure to get download URL
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle failure to upload image
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileCoverPhoto(Uri downloadUri) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(profileOrCoverPhoto, downloadUri.toString());

        databaseReference.child(user.getUid()).updateChildren(result)
                .addOnSuccessListener(unused -> {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Error Updating Image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        if(profileOrCoverPhoto.equals("image")){
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
            Query query=ref.orderByChild("uid").equalTo(uid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren()){
                        String child=ds.getKey();
                        snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }





    private void pickFromGallery() {
       Intent galleryIntent=new Intent(Intent.ACTION_PICK);
       galleryIntent.setType("image/*");
       startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera() {
        ContentValues values =new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null) {
//            mProfileTV.setText(user.getEmail());
            uid=user.getUid();
        }
        else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchMyPosts(query);
                }
                else{
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchMyPosts(newText);
                }
                else{
                    loadMyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(),AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }



}