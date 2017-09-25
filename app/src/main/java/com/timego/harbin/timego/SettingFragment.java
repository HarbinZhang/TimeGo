package com.timego.harbin.timego;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.timego.harbin.timego.database.Record;
import com.timego.harbin.timego.database.RecordContract;
import com.timego.harbin.timego.database.RecordDbHelper;

import static com.timego.harbin.timego.MainActivity.curtIndex;
import static com.timego.harbin.timego.MainActivity.editor;
import static com.timego.harbin.timego.MainActivity.mDatabase;
import static com.timego.harbin.timego.MainActivity.mFirebaseAuth;
import static com.timego.harbin.timego.MainActivity.mFirebaseUser;
import static com.timego.harbin.timego.MainActivity.mUserId;
import static com.timego.harbin.timego.MainActivity.prefs;


public class SettingFragment extends Fragment {


    private SQLiteDatabase mDb;

    private Button btn_timePicker, btn_editMore;
    private static TextView tv_wakeupTime;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_setting, container, false);


        // btn edit more
        btn_editMore = (Button) view.findViewById(R.id.btn_setting_edit_more);
        btn_editMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditMoreActivity.class);
                startActivity(intent);
            }
        });


        // btn sync
        Button btn_sync = (Button) view.findViewById(R.id.btn_setting_sync);
        tv_wakeupTime = (TextView) view.findViewById(R.id.tv_setting_wakeup_time);
        tv_wakeupTime.setText(timeToString(prefs.getInt("wakeupHour",0))+":"+
                timeToString(prefs.getInt("wakeupMinute",0)));
        RecordDbHelper dbHelper = new RecordDbHelper(getContext());
        mDb = dbHelper.getWritableDatabase();
        btn_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity)getActivity()).mFirebaseAuth != null){
                    syncRecord();
                }else{
                    Toast.makeText(getContext(), "You need to login first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // btn timepicker
        btn_timePicker = (Button) view.findViewById(R.id.btn_setting_wakeup);
        btn_timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });


//        timePicker = (TimePicker) view.findViewById(R.id.tp_setting_wakeup);
//        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                ((MainActivity)getActivity()).editor.putInt("wakeupHour", hourOfDay);
//                ((MainActivity)getActivity()).editor.putInt("wakeupMinute", minute);
//                ((MainActivity)getActivity()).editor.apply();
//            }
//        });


        return view;
    }



    private void syncRecord(){

        if(mFirebaseAuth == null || mFirebaseUser == null){
            Toast.makeText(getContext(), "You need to Sign in first.",Toast.LENGTH_LONG).show();
            return;
        }

        String selection = RecordContract.RecordEntry._ID + " >= " + String.valueOf(((MainActivity)getActivity()).curtIndex);
        Cursor cursor = mDb.query(RecordContract.RecordEntry.TABLE_NAME,
                null,
                selection,
                null,
                null,
                null,
                RecordContract.RecordEntry._ID
                );

        Long id = 0L;

        if (cursor.moveToFirst()){
            do{
                String type = cursor.getString(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_TYPE));
                int duration = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DURATION));
                int year = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_YEAR));
                int month = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_MONTH));
                int day = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DAY));
                String startTime = cursor.getString(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_STARTTIME));
                int efficient = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_EFFICIENT));
                Record record = new Record(type, duration, year, month, day, startTime, efficient);

                id = cursor.getLong(cursor.getColumnIndex(RecordContract.RecordEntry._ID));

                String uid = ((MainActivity)getActivity()).mUserId;
                ((MainActivity)getActivity()).mDatabase.child("users").child(uid).child("records").child(String.valueOf(id)).setValue(record);
            }while(cursor.moveToNext());
        }
        ((MainActivity)getActivity()).curtIndex = safeLongToInt(id);
        mDatabase.child("users").child(mUserId).child("curtIndex").setValue(curtIndex);
        Toast.makeText(getContext(),"Sync successful.", Toast.LENGTH_LONG).show();
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = prefs.getInt("wakeupHour", 0);
            int minute = prefs.getInt("wakeupMinute", 0);


            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            editor.putInt("wakeupHour", hourOfDay);
            editor.putInt("wakeupMinute", minute);
            editor.apply();
            tv_wakeupTime.setText(timeToString(hourOfDay)+":"+
                timeToString(minute));
        }
    }

//    public void showTimePicker(){
//        DialogFragment newFragment = new TimePickerFragment();
//        newFragment.show(getFragmentManager(), "timePicker");
//    }

    private static String timeToString(int t){
        if (t<10){
            return "0"+String.valueOf(t);
        }else{
            return String.valueOf(t);
        }
    }


}
