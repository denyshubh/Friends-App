package com.knstech.friendsapp2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPageAdapter;

    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String TAG = "Permission : ";

        Log.d(TAG,"onRequestPermission is Working");
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            File mFolder = new File(Environment.getExternalStorageDirectory(),"Friends_App");
            String path = mFolder.getPath();
            File audioDir = new File(path,"AUDIO");
            File videoDir = new File(path,"VIDEO");
            File imageDir = new File(path,"IMAGE");
            File docDir = new File(path,"DOC");
            if(!mFolder.exists()){
                boolean b = mFolder.mkdir();
                boolean b1 = audioDir.mkdir();
                boolean b2 = videoDir.mkdir();
                boolean b3 =imageDir.mkdir();
                boolean b4 =docDir.mkdir();

            }
            else
                Log.d(TAG,"onRequestPermission internal If is not working");
        }else
            Log.d(TAG,"onRequestPermission external if is not Working");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar= (android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends App");


        //---------------- CREATING DIRECTORY -------------------------------------------------------------------------------

        String TAG = "Permission : ";
        if(Build.VERSION.SDK_INT >=23){

            if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.v(TAG, " PERMISSION IS GRANTED");
                String p[]={"Permission Granted"};
                int r[]={PackageManager.PERMISSION_GRANTED,PackageManager.PERMISSION_DENIED};
                onRequestPermissionsResult(1,p,r);

            }

            else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

            }
        }

        else{
            Log.v(TAG,"PERMISSION IS AUTOMATICALLY GRANTED");
            String p[]={"Permission Granted"};
            int r[]={PackageManager.PERMISSION_GRANTED,PackageManager.PERMISSION_DENIED};
            onRequestPermissionsResult(1,p,r);
        }

   //     ---------------------------------------------------------------------------------------------------------------------




        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        // tabs
        mViewPager=(ViewPager)findViewById(R.id.main_tabPager);
        mSectionPageAdapter= new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionPageAdapter);

        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            setToStart();
        }
        else{

            mUserRef.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            //mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void setToStart() {
        startActivity(new Intent(MainActivity.this,StartActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         int id=item.getItemId();
         switch(id){

             case  R.id.main_logout_btn:
                                         FirebaseAuth.getInstance().signOut();
                                         setToStart();

                                         break;

             case R.id.main_settings_btn:
                                         startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                                         break;

             case  R.id.main_all_btn:
                                         startActivity(new Intent(MainActivity.this,UsersActivity.class));
                                         break;
         }



    return true;
    }

}
