package com.knstech.friendsapp2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText et_display_name,et_email,et_password;
    private Button mCreateBtn;
    private Toolbar mToolbar;
    private ProgressDialog progressDialog;

    // firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
// firebase Auth
        mAuth=FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar= (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog=new ProgressDialog(this);



        et_display_name= findViewById(R.id.et_display_name);
        et_email= findViewById(R.id.et_email);
      et_password= findViewById(R.id.et_pwd);
        mCreateBtn=(Button) findViewById(R.id.btn_create_account);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName,email,pwd;
                displayName=et_display_name.getText().toString();
                email=et_email.getText().toString();
                pwd=et_password.getText().toString();

                if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(pwd))
                {
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please Wait While Your Account Is Created !");
                    progressDialog.setCanceledOnTouchOutside(false);

                    progressDialog.show();
                    register_user(displayName, email, pwd);
                }


            }
        });

    }

    private void register_user(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid=current_user.getUid();

                            mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String,String> userMap=new HashMap<>();
                            userMap.put("name",displayName);
                            userMap.put("status","Hi there I'm using Friends App ");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        progressDialog.dismiss();
                                        // Sign in success, update UI with the signed-in user's information

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        String currentUserId=mAuth.getCurrentUser().getUid();
                                        databaseReference.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(mainIntent);
                                                    finish();

                                                }
                                            }
                                        });

                                        /**/

                                    }
                                }
                            });








                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot Sign In. Please check the form and try again.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
