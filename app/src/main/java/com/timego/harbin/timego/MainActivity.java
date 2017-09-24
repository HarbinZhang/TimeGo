package com.timego.harbin.timego;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import com.timego.harbin.timego.database.RecordContract;
import com.timego.harbin.timego.database.RecordDbHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected static FirebaseAuth mFirebaseAuth;
    protected static FirebaseUser mFirebaseUser;
    protected static DatabaseReference mDatabase;
    protected static String mUserId;


    protected static SQLiteDatabase mDb;

    private Fragment fragment;
    private String curtFragment;

    protected static SharedPreferences prefs;
    protected static SharedPreferences.Editor editor;

    private Context mContext;

    protected static int curtIndex;

    protected static Map<String,Integer> timeSum = new HashMap<>();

    private FragmentTransaction transaction;

//    protected static Map<String, String> moreActivities2Color = new HashMap<>();

    protected static JSONObject moreActivities2Color;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new AddRecordFragment();
                    curtFragment = "home";
                    invalidateOptionsMenu();
                    break;
                case R.id.navigation_dashboard:
                    prepareTodayTimeSum();
                    fragment = new DisplayPieFragment();
                    curtFragment = "dashboard";
                    invalidateOptionsMenu();
                    break;
                case R.id.navigation_setting:
                    fragment = new SettingFragment();
                    curtFragment = "setting";
                    invalidateOptionsMenu();
                    break;
            }
//            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction = getSupportFragmentManager().beginTransaction();
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

        // Prepare for moreActivities
        String moreActivities2Color_str = prefs.getString("moreActivities", "{\"more\":\"#FFFFFF\"}");
        try {
            moreActivities2Color = new JSONObject(moreActivities2Color_str);

        }catch (Exception e){

        }


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        fragment = new AddRecordFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, fragment).commit();
        curtFragment = "home";

        mContext = this;

        RecordDbHelper dbHelper = new RecordDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

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
                Toast.makeText(mContext, "Logout successful.", Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    invalidateOptionsMenu();
                }
                break;
            case R.id.menu_about:
                showAbout();
                break;
            case R.id.menu_today_pei:
                prepareTodayTimeSum();
                fragment = new DisplayPieFragment();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();
                break;
            case R.id.menu_week_pie:
                prepareWeekTimeSum();
                fragment = new DisplayPieFragment();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();
                break;
            case R.id.menu_month_pie:
                prepareMonthTimeSum();
                fragment = new DisplayPieFragment();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();
                break;
            case R.id.menu_week_line:
                fragment = new DisplayLineFragment();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void firebase_curtIndex(){
        Query curtIndexQuery = mDatabase.child("users").child(mFirebaseUser.getUid()).child("curtIndex");
        curtIndexQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String time = f.format(cal.getTime());

                    mDatabase.child("users").child(mUserId).child("newestTime").setValue(time);
                    mDatabase.child("users").child(mUserId).child("curtIndex").setValue(0);

                    curtIndex = 0;
                }else {
                    curtIndex = dataSnapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showAbout(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("About TimeGo");
        String info = "Happy finding you here : ) \n\n\n\n\n";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            info += "\t\t\tTimeGo\t\t\t" + pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        alertDialog.setMessage(info);
        alertDialog.setCancelable(true);
//        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        switch (curtFragment){
            case "home":
                menu.findItem(R.id.menu_about).setVisible(true);
                menu.findItem(R.id.menu_feedback).setVisible(true);
                menu.findItem(R.id.menu_today_pei).setVisible(false);
                menu.findItem(R.id.menu_week_pie).setVisible(false);
                menu.findItem(R.id.menu_month_pie).setVisible(false);
                menu.findItem(R.id.menu_week_line).setVisible(false);
//                menu.findItem(R.id.menu_signin).setVisible(false);
//                menu.findItem(R.id.menu_signup).setVisible(false);
//                menu.findItem(R.id.menu_logout).setVisible(false);
                if(mFirebaseUser != null){
                    menu.findItem(R.id.menu_logout).setVisible(false);
                }else{
                    menu.findItem(R.id.menu_signin).setVisible(false);
                    menu.findItem(R.id.menu_signup).setVisible(false);
                }

                break;
            case "dashboard":
                menu.findItem(R.id.menu_about).setVisible(false);
                menu.findItem(R.id.menu_feedback).setVisible(false);
                menu.findItem(R.id.menu_today_pei).setVisible(true);
                menu.findItem(R.id.menu_week_pie).setVisible(true);
                menu.findItem(R.id.menu_month_pie).setVisible(true);
//                menu.findItem(R.id.menu_signin).setVisible(false);
//                menu.findItem(R.id.menu_signup).setVisible(false);
//                menu.findItem(R.id.menu_logout).setVisible(false);
                if(mFirebaseUser != null){
                    menu.findItem(R.id.menu_logout).setVisible(false);
                }else{
                    menu.findItem(R.id.menu_signin).setVisible(false);
                    menu.findItem(R.id.menu_signup).setVisible(false);
                }

                break;
            case "setting":
                menu.findItem(R.id.menu_about).setVisible(true);
                menu.findItem(R.id.menu_feedback).setVisible(true);
                menu.findItem(R.id.menu_today_pei).setVisible(false);
                menu.findItem(R.id.menu_week_pie).setVisible(false);
                menu.findItem(R.id.menu_month_pie).setVisible(false);
                menu.findItem(R.id.menu_week_line).setVisible(false);
//                menu.findItem(R.id.menu_signin).setVisible();
//                menu.findItem(R.id.menu_signup).setVisible(false);
//                menu.findItem(R.id.menu_logout).setVisible(false);
                if(mFirebaseUser != null){
                    menu.findItem(R.id.menu_logout).setVisible(true);
                }else{
                    menu.findItem(R.id.menu_signin).setVisible(true);
                    menu.findItem(R.id.menu_signup).setVisible(true);
                }

                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private Cursor getRecords(String year, String month, String day){
        String whereCluse = RecordContract.RecordEntry.COLUMN_YEAR + " = '" + year + "' AND " +
                RecordContract.RecordEntry.COLUMN_MONTH + " = '" + month + "' AND " +
                RecordContract.RecordEntry.COLUMN_DAY + " = '" + day + "' ";
        return mDb.query(
                RecordContract.RecordEntry.TABLE_NAME,
                null,
                whereCluse,
                null,
                null,
                null,
                null
        );
    }


    private Cursor getTodayRecords(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(calendar.getTime());
        String[] dates = date.split("-");

        return getRecords(dates[0], dates[1], dates[2]);
    }

    protected static void mapInit(){
        timeSum.clear();
        timeSum.put("study",0);
        timeSum.put("entertain",0);
        timeSum.put("sleep",0);
        timeSum.put("exercise",0);
        timeSum.put("waste",0);
        timeSum.put("more", 0);
    }

    private void prepareTodayTimeSum(){
        mapInit();

        Cursor cursor = getTodayRecords();
        if (cursor != null){
            if(cursor.moveToFirst()){
                do{
                    int curtDuration = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DURATION));
                    String type = cursor.getString(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_TYPE));
                    timeSum.put(type, curtDuration + timeSum.get(type));
                }while(cursor.moveToNext());
            }
        }
    }

    private void prepareWeekTimeSum(){
        mapInit();

        for (int i = 0; i < 7; i++){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH,-i);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String date = df.format(calendar.getTime());
            String[] dates = date.split("-");

            Cursor cursor = getRecords(dates[0], dates[1], dates[2]);
            if (cursor != null){
                if(cursor.moveToFirst()){
                    do{
                        int curtDuration = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DURATION));
                        String type = cursor.getString(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_TYPE));
                        timeSum.put(type, curtDuration + timeSum.get(type));
                    }while(cursor.moveToNext());
                }
            }
        }
    }

    private void prepareMonthTimeSum(){
        mapInit();

        for (int i = 0; i < 30; i++){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH,-i);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String date = df.format(calendar.getTime());
            String[] dates = date.split("-");

            Cursor cursor = getRecords(dates[0], dates[1], dates[2]);
            if (cursor != null){
                if(cursor.moveToFirst()){
                    do{
                        int curtDuration = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DURATION));
                        String type = cursor.getString(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_TYPE));
                        timeSum.put(type, curtDuration + timeSum.get(type));
                    }while(cursor.moveToNext());
                }
            }
        }
    }

}
