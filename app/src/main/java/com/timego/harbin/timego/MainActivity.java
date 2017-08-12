package com.timego.harbin.timego;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    protected static FirebaseAuth mFirebaseAuth;
    protected static FirebaseUser mFirebaseUser;
    protected static DatabaseReference mDatabase;
    protected static String mUserId;

    private Fragment fragment;

    protected static SharedPreferences prefs;
    protected static SharedPreferences.Editor editor;

    private Context mContext;

    protected static int curtIndex;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new AddRecordFragment();
                    break;
                case R.id.navigation_dashboard:
                    fragment = new DisplayFragment();
                    break;
                case R.id.navigation_notifications:
                    fragment = new SettingFragment();
                    break;
            }
//            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment).commit();
//            transaction.add(R.id.main_container, fragment);
//            transaction.commit();
            return true;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        // SharedPreference
        prefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = prefs.edit();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        fragment = new AddRecordFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, fragment).commit();

        mContext = this;

//        if (mFirebaseUser == null) {
//            // Not logged in, launch the Log In activity
//            loadLogInView();
//        }else{
//            mUserId = mFirebaseUser.getUid();
//
//            fragment = new AddRecordFragment();
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.add(R.id.main_container, fragment).commit();
//
//        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(mFirebaseUser == null){
            menu.findItem(R.id.menu_logout).setVisible(false);
        }else{
            menu.findItem(R.id.menu_signin).setVisible(false);
            menu.findItem(R.id.menu_signup).setVisible(false);
        }

        return true;
    }

    @Override
    protected void onStart() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }

        if(mFirebaseAuth != null){
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            if (mFirebaseUser != null) {
                mUserId = mFirebaseUser.getUid();
                firebase_curtIndex();
            }
        }


        super.onStart();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_feedback:
                Toast.makeText(mContext, "Still in progress", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_signin:
                startActivity(new Intent(mContext, LogInActivity.class));
                break;
            case R.id.menu_signup:
                startActivity(new Intent(mContext, SignUpActivity.class));
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                mFirebaseUser = null;
                mFirebaseAuth = null;

                Toast.makeText(mContext, "Logout successful.", Toast.LENGTH_SHORT).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    invalidateOptionsMenu();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void firebase_curtIndex(){
        Query curtIndexQuery = mDatabase.child("users").child(mFirebaseUser.getUid()).child("curtIndex");
        curtIndexQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                curtIndex = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




}
