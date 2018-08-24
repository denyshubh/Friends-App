package com.knstech.friendsapp2;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StatusActivity extends AppCompatActivity {

        private Toolbar mToolbar;
        private Button mSavebtn;
        private EditText mStatus;
        private DatabaseReference mStatusDatabase;
        private FirebaseUser current_user;
        private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar=(Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSavebtn=(Button)findViewById(R.id.btn_save_Changes);
        mStatus=findViewById(R.id.et_status_input);
        String current_status_value=getIntent().getStringExtra("status_value");
        mStatus.setText(current_status_value);

        //Firebase

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String uid=current_user.getUid();
        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);



        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                mProgress=new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the Changes");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();


                String status=mStatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                        }
                        else{
                            Toast.makeText(StatusActivity.this, "Error Occured while saving Changes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }
}
