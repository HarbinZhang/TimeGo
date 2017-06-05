package com.timego.harbin.timego;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.timego.harbin.timego.database.Record;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;


public class AddRecordFragment extends Fragment {

    private TextView info_tv,duration_tv;
    private Button hour_btn, min_btn, ok_btn, reset_btn;
    private RadioGroup radGroup, eff_radGroup;
    private int duration;
    private String curt_activity = "study";
    private int efficient = 4;
    private Queue<Record> recordqueue = new LinkedList<>();

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;
    private FirebaseAuth mFirebaseAuth;

    public AddRecordFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserId = mFirebaseUser.getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_record, container, false);

        initForXml(view);

        return view;
    }


    private void initForXml(View view){
        // xml
        eff_radGroup = (RadioGroup) view.findViewById(R.id.main_eff_group);
        radGroup = (RadioGroup) view.findViewById(R.id.main_radioGroup);
        hour_btn = (Button) view.findViewById(R.id.main_hour_btn);
        min_btn = (Button) view.findViewById(R.id.main_min_btn);
        ok_btn = (Button) view.findViewById(R.id.main_ok_btn);
        reset_btn = (Button) view.findViewById(R.id.main_reset_btn);
        duration_tv = (TextView) view.findViewById(R.id.main_duration_tv);
        info_tv = (TextView) view.findViewById(R.id.main_info_tv);
        duration = 0;
        duration_tv.setText(String.valueOf(duration)+" min");
        radGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.study_btn:
                        saveTempData();
                        curt_activity = "study";
                        duration = 0;
                        duration_tv.setText(String.valueOf(duration)+" min");
                        break;
                    case R.id.entertain_btn:
                        saveTempData();
                        curt_activity = "entertain";
                        duration = 0;
                        duration_tv.setText(String.valueOf(duration)+" min");
                        break;
                    case R.id.sleep_btn:
                        saveTempData();
                        curt_activity = "sleep";
                        duration = 0;
                        duration_tv.setText(String.valueOf(duration)+" min");
                        break;
                    case R.id.exercise_btn:
                        saveTempData();
                        curt_activity = "exercise";
                        duration = 0;
                        duration_tv.setText(String.valueOf(duration)+" min");
                        break;
                }

            }
        });

        eff_radGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.eff_5_btn:
                        efficient = 5;
                        break;
                    case R.id.eff_4_btn:
                        efficient = 4;
                        break;
                    case R.id.eff_3_btn:
                        efficient = 3;
                        break;
                    case R.id.eff_2_btn:
                        efficient = 2;
                        break;
                    case R.id.eff_1_btn:
                        efficient = 1;
                        break;
                }
            }
        });

        hour_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duration = duration + 60;
                duration_tv.setText(String.valueOf(duration)+" min");
            }
        });

        min_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duration = duration + 10;
                duration_tv.setText(String.valueOf(duration)+" min");
            }
        });

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(duration != 0){
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String date = df.format(calendar.getTime());

                    Record record = new Record(curt_activity, duration, date, efficient);
                    recordqueue.add(record);
                }
                while(!recordqueue.isEmpty()){
                    Record record = recordqueue.poll();
                    mDatabase.child("users").child(mUserId).child("records").push().setValue(record);
                }
                duration = 0;
                duration_tv.setText(String.valueOf(duration) + " min");
            }
        });

        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordqueue.clear();
                duration = 0;
                duration_tv.setText(String.valueOf(duration) + " min");
            }
        });
    }


    private void saveTempData(){
        if(duration == 0){
            return;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(calendar.getTime());

        Record record = new Record(curt_activity, duration, date, efficient);
        recordqueue.add(record);
        info_tv.setText(mFirebaseUser.getUid());
    }
}
