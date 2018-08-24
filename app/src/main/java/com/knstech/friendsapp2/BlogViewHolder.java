package com.knstech.friendsapp2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context context;
    ImageButton mLikeBtn;
    ImageView postImage;
    TextView count;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    public BlogViewHolder(View itemView) {

        super(itemView);
        this.mView = itemView;
        this.mLikeBtn = itemView.findViewById(R.id.btn_like);
        this.postImage = itemView.findViewById(R.id.post_image);
        this.count = itemView.findViewById(R.id.likes_count);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
        databaseReference.keepSynced(true);
    }


    public void setLikeBtns(final String post_key){

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long c = dataSnapshot.child(post_key).getChildrenCount();
               if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){

                    mLikeBtn.setImageResource(R.drawable.like_1);
                    count.setText(c+" Likes");
                }
                else{
                    mLikeBtn.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setContext(Context context){
        this.context = context;

    }

    public void setTitle(String title){

        TextView  post_title = (TextView)mView.findViewById(R.id.post_title);
        post_title.setText(title);

    }

    public void setDesc(String desc){
        TextView  post_desc = (TextView)mView.findViewById(R.id.post_desc);
        post_desc.setText(desc);
    }

    public void setImage(String download_url){

        ImageView post_img = (ImageView)mView.findViewById(R.id.post_image);
        //Picasso.get().load(download_url).placeholder(R.drawable.propic).into(post_img);

       RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.logo)
                .error(R.drawable.msgback3)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontTransform();
        Glide.with(context)
                .load(download_url)
                .apply(options)
                .into(post_img);

    }

    public void setUserImage(String img){

        ImageView post_img = (ImageView)mView.findViewById(R.id.post_user_profile_pic);
       //Picasso.get().load(img).placeholder(R.drawable.propic).into(post_img);
        Glide.with(itemView).load(img).into(post_img);

    }

    public void setUsername(String username){

        TextView  post_desc = (TextView)mView.findViewById(R.id.post_user_name);
        post_desc.setText(username);
    }

    public void setTime(Object time){

        String timeStamp = time.toString();

        GetTimeAgo getTimeAgo = new GetTimeAgo();

        long lastTime = Long.parseLong(timeStamp);

        String blogPostTime = getTimeAgo.getTimeAgo(lastTime, mView.getContext());

        TextView  post_desc = (TextView)mView.findViewById(R.id.post_time);
        post_desc.setText(blogPostTime);
    }



}
