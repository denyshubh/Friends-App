package com.knstech.friendsapp2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersViewHolder extends RecyclerView.ViewHolder {

    View mView;
    public UsersViewHolder(View itemView) {
        super(itemView);

        mView=itemView;

    }
    public void setName(String name){
        TextView mUserNameViewc = (TextView) mView.findViewById(R.id.tv_users_name);
        mUserNameViewc.setText(name);
    }
    public void setStatus(String status){
        TextView mUserStatusView = (TextView) mView.findViewById(R.id.tv_users_status);
        mUserStatusView.setText(status);
    }

    public void setUserImage(String thumb_image, Context context){
        CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.users_cv_propic);
        Picasso.get().load(thumb_image).placeholder(R.drawable.propic).into(userImageView);
    }
}
