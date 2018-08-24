package com.knstech.friendsapp2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase,mUserRef;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;

    private CircleImageView mImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusbtn,mImagebtn;
    private final int GALLERY_PIC=1;
    private static String image;

    //Database Storage
    private StorageReference mProPic;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mImage=(CircleImageView)findViewById(R.id.settings_image);
        mName=findViewById(R.id.tv_display_name);
        mStatus=findViewById(R.id.tv_about);

        // pointing to root of our storage
        mProPic = FirebaseStorage.getInstance().getReference();

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=mCurrentUser.getUid();

        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                 image=dataSnapshot.child("image").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!image.equals("default"))

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.propic).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.propic).into(mImage);

                        }
                    });




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mStatusbtn=(Button)findViewById(R.id.btn_change_status);
        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value=mStatus.getText().toString();
                Intent intent=new Intent(SettingsActivity.this,StatusActivity.class);
                intent.putExtra("status_value",status_value);
                startActivity(intent);
            }
        });

        mImagebtn=(Button)findViewById(R.id.btn_change_image);
        mImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PIC);
                */

                // start picker to get image for cropping and then use the image in cropping activity

                CropImage.activity().setAspectRatio(1,1).setFixAspectRatio(true)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog=new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please Wait while we process the image");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                final File thumb_filepath=new File(resultUri.getPath());

                String uid=mCurrentUser.getUid();


                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    final byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath= mProPic.child("profile_images").child(uid+".jpg");
                final StorageReference thumbfirebase_filepath= mProPic.child("profile_images").child("thumbs").child(uid+"jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            final String download_url=task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask= thumbfirebase_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl=thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()){
                                        Map update_hashmap=new HashMap<>();
                                        update_hashmap.put("image",download_url);
                                        update_hashmap.put("thumb_image",thumb_downloadUrl);

                                        mUserDatabase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Uploading Succesfull", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Error in Uploading thumbnail", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error in Uploading", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                String imageUri=resultUri.toString();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

}
