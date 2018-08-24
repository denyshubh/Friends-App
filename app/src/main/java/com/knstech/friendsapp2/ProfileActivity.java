package com.knstech.friendsapp2;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {


    private String uid;
    private ImageView mprofileImageView;
    private TextView mProfileName,mProfileStatus,mProfileFriends;
    private Button mprofileSendFriendRequestbtn;
    private Button mprofileDeclineRequestbtn;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private DatabaseReference mFriendRequestDatabase;
    private int current_status;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mfriendDatabase,mRootRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        mprofileSendFriendRequestbtn=(Button)findViewById(R.id.btn_profile_send_friend_request);
        mprofileDeclineRequestbtn=(Button)findViewById(R.id.btn_profile_decline_friend_request);
        mprofileImageView=(ImageView)findViewById(R.id.iv_profile_propic);
        mProfileName=(TextView)findViewById(R.id.tv_profile_name);
        mProfileStatus=(TextView)findViewById(R.id.tv_profile_status);
        mProfileFriends=(TextView)findViewById(R.id.tv_profile_friendsCount);
        uid = getIntent().getStringExtra("uid");

        mprofileDeclineRequestbtn.setVisibility(View.INVISIBLE);
        mprofileDeclineRequestbtn.setEnabled(false);


        // getting Firebase  user refrence
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mfriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");

        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");

        mRootRef=FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        current_status=0; // Not Friends

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load user data...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name= dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                //Picasso.get().load(image).placeholder(R.drawable.propic).into(mprofileImageView);

                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.msgback3)
                        .error(R.drawable.back)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH)
                        .dontTransform();
                Glide.with(getApplicationContext())
                        .load(image)
                        .apply(options)
                        .into(mprofileImageView);

                if(mCurrentUser.getUid().equals(uid)){

                    mprofileDeclineRequestbtn.setEnabled(false);
                    mprofileDeclineRequestbtn.setVisibility(View.INVISIBLE);

                    mprofileSendFriendRequestbtn.setEnabled(false);
                    mprofileSendFriendRequestbtn.setVisibility(View.INVISIBLE);

                }


                //-------------- Friend List / Request Feature -------------------------

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(uid)){
                            String req_type=dataSnapshot.child(uid).child("request_type").getValue().toString();

                            if(req_type.equals("received")){
                                current_status=2;   // Request Received
                                mprofileSendFriendRequestbtn.setText("Accept Friend Request");
                                mprofileDeclineRequestbtn.setVisibility(View.VISIBLE);
                                mprofileDeclineRequestbtn.setEnabled(false);

                            }
                            else if(req_type.equals("sent")){
                                current_status=3;   // Cancel Request
                                mprofileSendFriendRequestbtn.setText("Cancel Friend Request");
                                mprofileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                mprofileDeclineRequestbtn.setEnabled(false);
                            }
                            progressDialog.dismiss();

                        }

                        else{

                            mfriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(uid)){

                                        current_status=4;   // FRIENDS
                                        mprofileSendFriendRequestbtn.setText("UNFRIEND");

                                        mprofileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                        mprofileDeclineRequestbtn.setEnabled(false);

                                    }
                                    progressDialog.dismiss();

                                    if(dataSnapshot.exists()){

                                        int count =0;
                                        count = (int) dataSnapshot.getChildrenCount();
                                        mProfileFriends.setText(count + "  Friends   |   x  Mutual Friends");
                                    }
                                    else{
                                        mProfileFriends.setText("0  Friends   |   0  Mutual Friends");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mprofileSendFriendRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprofileSendFriendRequestbtn.setEnabled(false);

                // ------------------- Current State if " Friend Request Not Sent " SEND FRIEND REQUEST/ NOT FRIEND STATE---------------------
                if(current_status == 0){

                    DatabaseReference newNotification= mRootRef.child("notifications").child(uid).push();
                  String newNotificationId = newNotification.getKey();


                            HashMap<String,String> notificationData=new HashMap<>();
                            notificationData.put("from",mCurrentUser.getUid());
                            notificationData.put("type","request");

                            Map requestMap= new HashMap();
                            requestMap.put("Friend_req/"+ mCurrentUser.getUid() + "/" + uid +"/" + "request_type", "sent");
                            requestMap.put("Friend_req/"+uid + "/" + mCurrentUser.getUid() +"/" + "request_type", "received");
                            requestMap.put("notifications/"+ uid + "/" + newNotificationId,notificationData );

                            mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError != null){
                                        Toast.makeText(ProfileActivity.this, "there was some error in sending request", Toast.LENGTH_SHORT).show();
                                    }

                                   else {
                                        current_status = 1;   // Request Sent
                                        mprofileSendFriendRequestbtn.setText("Cancel Friend Request");

                                    }

                                    mprofileSendFriendRequestbtn.setEnabled(true);


                                }
                            });


                }



                //-------------------- CANCEL FRIEND REQUEST ---------------------------------

                if(current_status==1){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(uid).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(uid).child(mCurrentUser.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mprofileSendFriendRequestbtn.setEnabled(true);
                                            current_status=0;   // Request Sent
                                            mprofileSendFriendRequestbtn.setText("Send Friend Request");

                                            mprofileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                            mprofileDeclineRequestbtn.setEnabled(false);

                                        }
                                    });
                                }
                            });
                }

                //------------------------- Request Recieved State ------------------

                if(current_status == 2){

                    final String currentDate=DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap=new HashMap();
                    friendsMap.put("Friends/"+mCurrentUser.getUid()+"/"+uid+"/date",currentDate);
                    friendsMap.put("Friends/"+uid+"/"+mCurrentUser.getUid()+"/date",currentDate);

                    friendsMap.put("Friend_req/"+ mCurrentUser.getUid() + "/" + uid, null);
                    friendsMap.put("Friend_req/"+uid + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mprofileSendFriendRequestbtn.setEnabled(true);
                                current_status=4;   // FRIENDS
                                mprofileSendFriendRequestbtn.setText("UNFRIEND");

                                mprofileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                mprofileDeclineRequestbtn.setEnabled(false);
                            }
                            else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }
                        }
                    });


                }

                //--------------------- UNFRIEND -----------------------------

                   if(current_status==4) {


                       Map unfriendMap=new HashMap();
                       unfriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+uid+"/date",null);
                       unfriendMap.put("Friends/"+uid+"/"+mCurrentUser.getUid()+"/date",null);

                       mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                           @Override
                           public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                               if(databaseError == null){

                                  current_status=0;
                                   mprofileSendFriendRequestbtn.setText("Send Friend Request");

                                   mprofileDeclineRequestbtn.setVisibility(View.INVISIBLE);
                                   mprofileDeclineRequestbtn.setEnabled(false);


                               }
                               else{
                                   String error = databaseError.getMessage();
                                   Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                               }

                              mprofileSendFriendRequestbtn.setEnabled(true);



                           }
                       });


                   }



            }
        });



    }
}
