package com.knstech.friendsapp2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
public class PhoneAuthActivity extends AppCompatActivity {

    private Button btnGenerateOTP, btnSignIn;
    private EditText etPhoneNumber, etOTP;

    private String phoneNumber, otp, country_code;
    private EditText displayName;

    private Toolbar mToolbar;
    private ProgressDialog progressDialog;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private  CountryCodePicker cc;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        mToolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Phone Login");

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();

        findViews();

        progressDialog=new ProgressDialog(this);


        btnSignIn.setVisibility(View.INVISIBLE);
        etOTP.setEnabled(false);
        btnSignIn.setVisibility(View.INVISIBLE);
        btnSignIn.setEnabled(false);

        StartFirebaseLogin();

        btnGenerateOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber= cc.getSelectedCountryCode()+etPhoneNumber.getText().toString();



                if(!TextUtils.isEmpty(phoneNumber)){

                    progressDialog.setTitle("Sending The OTP");
                    progressDialog.setMessage("Please wait while we check your credentials !");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,                     // Phone number to verify
                            60,                           // Timeout duration
                            TimeUnit.SECONDS,                // Unit of timeout
                            PhoneAuthActivity.this,        // Activity (for callback binding)
                            mCallback);                      // OnVerificationStateChangedCallbacks
                }


            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setTitle("Sign In");
                progressDialog.setMessage("Please wait while we check your credentials !");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                otp=etOTP.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                SigninWithPhone(credential);
            }
        });
    }
    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid=current_user.getUid();
                            DatabaseReference mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", displayName.getText().toString());
                            userMap.put("status", "Hi there I'm using Friends App ");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        progressDialog.dismiss();
                                        // Sign in success, update UI with the signed-in user's information

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        String currentUserId = auth.getCurrentUser().getUid();


                                        databaseReference.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    Intent mainIntent = new Intent(PhoneAuthActivity.this, MainActivity.class);
                                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(mainIntent);
                                                    finish();

                                                }
                                            }
                                        });
                                    } else {

                                        progressDialog.hide();

                                        String task_result = task.getException().getMessage().toString();

                                        Toast.makeText(PhoneAuthActivity.this, "Error : " + task_result, Toast.LENGTH_LONG).show();

                                    }

                                }
                            });
                        }
                    }
                });
    }
    private void findViews() {

        cc = (CountryCodePicker)findViewById(R.id.country_code);
        btnGenerateOTP=findViewById(R.id.btn_generate_otp);
        btnSignIn=findViewById(R.id.btn_phone_login);
        etPhoneNumber=findViewById(R.id.et_phone_number);
        etOTP=findViewById(R.id.et_otp);
        displayName=findViewById(R.id.et_phone_user_name);

    }
    private void StartFirebaseLogin() {


        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                progressDialog.dismiss();

            }
            @Override
            public void onVerificationFailed(FirebaseException e) {

                progressDialog.hide();

            }
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationCode = s;
                progressDialog.hide();

                Toast.makeText(PhoneAuthActivity.this, "Please Enter The Otp Send To Your Phone Number", Toast.LENGTH_SHORT).show();
                btnGenerateOTP.setEnabled(false);
                btnGenerateOTP.setVisibility(View.INVISIBLE);
                btnSignIn.setEnabled(true);
                btnSignIn.setVisibility(View.VISIBLE);
                etOTP.setEnabled(true);

            }
        };
    }
}