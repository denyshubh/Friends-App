package com.knstech.friendsapp2;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private String user_id;
    private boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike,mDatabaseUsers;
    private FirebaseAuth mAuth;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getContext(),NewPostActivity.class));

                /*   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mBlogList = (RecyclerView)view.findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(linearLayoutManager);



        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(

                Blog.class,
                R.layout.post_single_layout,
                BlogViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, final Blog model, int position) {


                viewHolder.setContext(getContext());
                final String list_blog_id = getRef(position).getKey();
                viewHolder.setLikeBtns(list_blog_id);

                Query lastMessageQuery = mDatabase.child(list_blog_id).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        viewHolder.setTitle(model.getTitle());
                        viewHolder.setDesc(model.getDesc());

                        viewHolder.setImage(model.getImage());

                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setUserImage(model.getThumb_image());
                        viewHolder.setTime(model.getTimestamp());
                        user_id = model.getUser_id();


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        Intent imageFullScreen = new Intent(getContext(),PhotoActivity.class);
                        imageFullScreen.putExtra("uid", list_blog_id);
                        imageFullScreen.putExtra("from","RequestsFragment");
                        startActivity(imageFullScreen);

                    }
                });

                viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        mProcessLike = true;

                            mDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(mProcessLike) {

                                        if (dataSnapshot.child(list_blog_id).hasChild(mAuth.getCurrentUser().getUid())) {

                                            mDatabaseLike.child(list_blog_id).child(mAuth.getCurrentUser().getUid()).removeValue();
                                            mProcessLike = false;

                                        } else {
                                            mDatabaseLike.child(list_blog_id).child(mAuth.getCurrentUser().getUid()).setValue("Lliked");
                                            mProcessLike = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                });

            }
        };



        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }





}
