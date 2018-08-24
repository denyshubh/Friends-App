package com.knstech.friendsapp2;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder{

    View mView;
    public FriendsViewHolder(View itemView) {
        super(itemView);
        mView=itemView;

    }
   public  void setDate(String date){
       TextView userNameView=(TextView) mView.findViewById(R.id.tv_users_status);
       userNameView.setText(date);
    }

    public void setName(String name){
        TextView userNameView=(TextView) mView.findViewById(R.id.tv_users_name);
        userNameView.setText(name);
    }

    public void setImage(String thumb_image, Context context){
        CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.users_cv_propic);
        Picasso.get().load(thumb_image).placeholder(R.drawable.propic).into(userImageView);
    }
    public void  setUserOnline(String online_status){
        ImageView userOnlineView=(ImageView)mView.findViewById(R.id.user_single_online_icon );
        if(online_status.equals("true")){
            userOnlineView.setVisibility(View.VISIBLE);
        }
        else{
            userOnlineView.setVisibility(View.INVISIBLE);
        }

    }
}
