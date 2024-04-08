package com.example.social;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.social.adapters.AdapterUser;
import com.example.social.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<ModelUser>userList;
    FirebaseAuth firebaseAuth;
    public UsersFragment(){

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_users,container,false);
        recyclerView=view.findViewById(R.id.users_RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseAuth=FirebaseAuth.getInstance();

        userList =new ArrayList<>();
        getAllUsers();

        return view;
    }

//    private void getAllUsers() {
//        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                userList.clear();
//                for(DataSnapshot ds: snapshot.getChildren()){
//                    ModelUser modelUser=ds.getValue(ModelUser.class);
//
//                    if(!modelUser.getUid().equals(fUser.getUid())){
//                        userList.add(modelUser);
//                    }
//                    adapterUser=new AdapterUser(getActivity(),userList);
//                    recyclerView.setAdapter(adapterUser);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void getAllUsers() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    if(!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }
                }
                // Set the adapter after adding all users to the list
                adapterUser = new AdapterUser(getActivity(), userList);
                recyclerView.setAdapter(adapterUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });
    }

//    private void searchUsers(String query) {
//
//        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                userList.clear();
//                for(DataSnapshot ds: snapshot.getChildren()){
//                    ModelUser modelUser=ds.getValue(ModelUser.class);
//
//                    if(!modelUser.getUid().equals(fUser.getUid())){
//                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
//                        modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
//                            userList.add(modelUser);
//                        }
//
//                    }
//                    adapterUser=new AdapterUser(getActivity(),userList);
//                    adapterUser.notifyDataSetChanged();
//                    recyclerView.setAdapter(adapterUser);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }

    private void searchUsers(String query) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    if(!modelUser.getUid().equals(fUser.getUid())){
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }
                    }
                }
                // Set the adapter after filtering the users based on the search query
                adapterUser = new AdapterUser(getActivity(), userList);
                adapterUser.notifyDataSetChanged(); // Notify adapter of data changes
                recyclerView.setAdapter(adapterUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });
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
        }
        else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        if (item != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (!TextUtils.isEmpty(query.trim())) {
                            searchUsers(query);
                        } else {
                            getAllUsers();
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (!TextUtils.isEmpty(newText.trim())) {
                            searchUsers(newText);
                        } else {
                            getAllUsers();
                        }
                        return false;
                    }
                });
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
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



    //    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_main,menu);
//        MenuItem item=menu.findItem(R.id.action_search);
//        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                if(!TextUtils.isEmpty(query.trim())){
//                    searchUsers(query);
//                }
//                else{
//                    getAllUsers();
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if(!TextUtils.isEmpty(newText.trim())){
//                    searchUsers(newText);
//                }
//                else{
//                    getAllUsers();
//                }
//                return false;
//            }
//        });
//        super.onCreateOptionsMenu(menu,inflater);
//    }

}