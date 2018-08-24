package com.knstech.friendsapp2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private TextView usersName,usersStatus;
    private CircleImageView mProPic;

    private DatabaseReference mUsersDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolbar=(Toolbar)findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList=(RecyclerView)findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        usersName=(TextView)findViewById(R.id.tv_users_name);
        usersStatus=(TextView)findViewById(R.id.tv_users_status);
        mProPic=(CircleImageView)findViewById(R.id.users_cv_propic);

    }

    @Override
    protected void onStart() {
        super.onStart();

FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
        Users.class,
        R.layout.users_single_layout,
        UsersViewHolder.class,
        mUsersDatabase

) {
    @Override
    protected void populateViewHolder(UsersViewHolder viewHolder, Users model, final int position) {
        viewHolder.setName(model.getName());
        viewHolder.setStatus(model.getStatus());
        viewHolder.setUserImage(model.getThumbImage(),getApplicationContext());
        final String uid=getRef(position).getKey(); // retrieve key of user that is in database

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profile_intent=new Intent(UsersActivity.this,ProfileActivity.class);
                profile_intent.putExtra("uid",uid);
                startActivity(profile_intent);
            }
        });
    }
};

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }
}
