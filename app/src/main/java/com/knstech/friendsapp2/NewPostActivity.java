package com.knstech.friendsapp2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mPostTitle,mPostDesc;
    private Button btnPost;
    private Uri mImageUri = null;

    private ProgressDialog progressDialog;

    private DatabaseReference mRootRef;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Friends Blog");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        storageReference= FirebaseStorage.getInstance().getReference();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();



    mSelectImage=(ImageButton)findViewById(R.id.blog_add_image_btn);
    mPostTitle=(EditText)findViewById(R.id.et_post_title);
    mPostDesc=(EditText)findViewById(R.id.et_post_desc);
    btnPost=(Button)findViewById(R.id.post_btn);
    progressDialog= new ProgressDialog(this);

    mSelectImage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            CropImage.activity().setAspectRatio(1,1)
                    .setFixAspectRatio(true)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(NewPostActivity.this);



        }
    });


    btnPost.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startPosting();
        }
    });

    }

    private void startPosting() {

        progressDialog=new ProgressDialog(NewPostActivity.this);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setMessage("Please Wait while we process the image");
        progressDialog.setCanceledOnTouchOutside(false);


        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val=mPostDesc.getText().toString().trim();


        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri!=null){

            progressDialog.show();
            String random_name= random();
            StorageReference filepath = storageReference.child("Blog_Images").child(random_name);
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPost = mRootRef.child("Blog").push();

                    final String user_id= mCurrentUser.getUid();

                    mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newPost.child("title").setValue(title_val);
                            newPost.child("desc").setValue(desc_val);

                            newPost.child("image").setValue(downloadUrl.toString());

                            newPost.child("user_id").setValue(user_id);
                            newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue());
                            newPost.child("thumb_image").setValue(dataSnapshot.child("thumb_image").getValue());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.dismiss();

                    startActivity(new Intent(NewPostActivity.this,MainActivity.class));
                    finish();


                }
            });



        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                 mImageUri = result.getUri();
                 mSelectImage.setImageURI(mImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


}
