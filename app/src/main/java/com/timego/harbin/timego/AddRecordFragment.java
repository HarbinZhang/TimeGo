package com.timego.harbin.timego;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.timego.harbin.timego.database.RecordContract;
import com.timego.harbin.timego.database.RecordDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static com.timego.harbin.timego.MainActivity.editor;
import static com.timego.harbin.timego.MainActivity.longTimeId;
import static com.timego.harbin.timego.MainActivity.mDb;
import static com.timego.harbin.timego.MainActivity.prefs;


public class AddRecordFragment extends Fragment{

    private TextView info_tv,duration_tv, tv_last_time, tv_duration_remain;
    private Button hour_btn, min_btn, add_btn, onemin_btn;
    private Button btn_reset, btn_undo;
    private RadioGroup radGroup, eff_radGroup;
    private int duration;
    private String curt_activity = "study";
    private int efficient = 4;


    private RecyclerView mRecyclerView;
    private RecordAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    private Calendar last_time_cal = Calendar.getInstance();
    private long duration_remain;


    private Stack<Integer> duration_stack;


    private Spinner sp_more;


    private ShowcaseView showcaseView;
    private int counter = 0;


    public AddRecordFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_record, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_record_container);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecordDbHelper dbHelper = new RecordDbHelper(getContext());
//        mDb = dbHelper.getWritableDatabase();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String[] dates = df.format(calendar.getTime()).split("-");

        Cursor cursor = getRecords(dates[0], dates[1], dates[2]);

        mAdapter = new RecordAdapter(getContext(), cursor);
        mRecyclerView.setAdapter(mAdapter);

        duration_stack = new Stack<Integer>();

        mRecyclerView.scrollToPosition(cursor.getCount()-1);

        initForXml(view);

        initLastTime();

        initShowcase(view);

        return view;
    }


    private void initShowcase(View view){
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(getResources().getDimension(R.dimen.font_normal));

//        paint.setTypeface(Typeface.createFromAsset(getAssets()))
        showcaseView = new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowcaseTheme2)
                .setTarget(new ViewTarget(view.findViewById(R.id.tv_setting_last_time)))
                .setContentTitle("Current recorded time")
                .setContentTextPaint(paint)
                .singleShot(longTimeId)
                .setContentText("It shows the newest recorded activity end time. \n " +
                        "Your new record will start at this time.")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (counter){
                            case 0:
                                showcaseView.setShowcase(new ViewTarget(tv_duration_remain), true);
                                showcaseView.setContentTitle("Remain duration");
                                showcaseView.setContentText("It shows how much minutes between recorded time and now.");
                                break;
                            case 1:
                                showcaseView.setShowcase(new ViewTarget(duration_tv), true);
                                showcaseView.setContentTitle("Current duration");
                                showcaseView.setContentText("It shows how much minutes your recording activity taken.");
                                break;
                            case 2:
                                showcaseView.setShowcase(new ViewTarget(radGroup), true);
                                showcaseView.setContentTitle("Current Activity");
                                showcaseView.setContentText("Choose your current activity type\n" +
                                        "Like self-improment, healthy activity or funny activity etc.");
                                break;
                            case 3:
                                showcaseView.setShowcase(new ViewTarget(eff_radGroup), true);
                                showcaseView.setContentTitle("Current Activity Efficiency");
                                showcaseView.setContentText("Choose your feeling about this Activity\n" +
                                        "If you think you are 100% concentrated on it, choose Perfect : ) ");
                                break;
                            case 4:
                                showcaseView.setShowcase(new ViewTarget(mRecyclerView),true);
                                showcaseView.setStyle(R.style.CustomShowcaseTheme);
                                showcaseView.setContentTitle("Activities show here");
                                showcaseView.setContentText("Added records would be shown here \n" +
                                        "If you want to remove them, just swipe it out.");
                                break;
                            case 5:
                                showcaseView.hide();
                                break;
                        }
                        counter++;
                    }
                })
                .build();
    }

    private void initForXml(View view){
        // xml
        eff_radGroup = (RadioGroup) view.findViewById(R.id.main_eff_group);
        radGroup = (RadioGroup) view.findViewById(R.id.main_radioGroup);
        hour_btn = (Button) view.findViewById(R.id.main_hour_btn);
        min_btn = (Button) view.findViewById(R.id.main_min_btn);
        add_btn = (Button) view.findViewById(R.id.main_ok_btn);
        onemin_btn = (Button) view.findViewById(R.id.main_onemin_btn);
        duration_tv = (TextView) view.findViewById(R.id.main_duration_tv);
        tv_last_time = (TextView) view.findViewById(R.id.tv_setting_last_time);
        tv_duration_remain = (TextView) view.findViewById(R.id.tv_setting_duration);
        btn_reset = (Button) view.findViewById(R.id.btn_record_reset);
        btn_undo = (Button) view.findViewById(R.id.btn_record_undo);

        // init spinner for "more" option
//        sp_more = (Spinner) view.findViewById(R.id.sp_add_record_more);
//        Iterator keysToCopyIterator = moreActivities2Color.keys();
//        List<String> keysList = new ArrayList<String>();
//        while(keysToCopyIterator.hasNext()) {
//            String key = (String) keysToCopyIterator.next();
//            keysList.add(key);
//        }
//        final String[] moreActivities = keysList.toArray(new String[keysList.size()]);

//        ArrayAdapter<String> moreAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, moreActivities);
//        sp_more.setAdapter(moreAdapter);
//        sp_more.setVisibility(View.VISIBLE);
//        sp_more.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                curt_activity = moreActivities[position];
//                radGroup.clearCheck();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


        duration = 0;
        duration_tv.setText(minToHour(duration));
        radGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.study_btn:
//                        if(!checkRemainDuration()){return;}
//                        addNewActivity();
                        curt_activity = "study";
                        break;
                    case R.id.entertain_btn:
//                        if(!checkRemainDuration()){return;}
//                        addNewActivity();
                        curt_activity = "entertain";
                        break;
                    case R.id.sleep_btn:
//                        if(!checkRemainDuration()){return;}
//                        addNewActivity();
                        curt_activity = "sleep";
                        break;
                    case R.id.exercise_btn:
//                        if(!checkRemainDuration()){return;}
//                        addNewActivity();
                        curt_activity = "exercise";
                        break;
                    case R.id.trash_btn:
//                        if(!checkRemainDuration()){return;}
//                        addNewActivity();
                        curt_activity = "waste";
                        break;
                    case R.id.more_btn:
                        curt_activity = "more";
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
                if (duration >= duration_remain){
                    Toast.makeText(getContext(), "Not enough time slot for the activity.", Toast.LENGTH_SHORT).show();
                    return;
                }else if(duration + 60 > duration_remain){
                    int actualTime = safeLongToInt(duration_remain) - duration;
                    duration_stack.push(actualTime);
                    duration += actualTime;
                    duration_tv.setText(minToHour(duration));
                }else {
                    duration_stack.push(60);
                    duration = duration + 60;
                    duration_tv.setText(minToHour(duration));
                }
            }
        });

        min_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (duration >= duration_remain){
                    Toast.makeText(getContext(), "Not enough time slot for the activity.", Toast.LENGTH_SHORT).show();
                    return;
                }else if(duration + 10 > duration_remain){
                    int actualTime = safeLongToInt(duration_remain) - duration;
                    duration_stack.push(actualTime);
                    duration += actualTime;
                    duration_tv.setText(minToHour(duration));
                }else {
                    duration_stack.push(10);
                    duration = duration + 10;
                    duration_tv.setText(minToHour(duration));
                }

            }
        });

        onemin_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (duration >= duration_remain){
                    Toast.makeText(getContext(), "Not enough time slot for the activity.", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    duration_stack.push(1);
                    duration += 1;
                    duration_tv.setText(minToHour(duration));
                }

            }
        });

        btn_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(duration_stack.isEmpty()){
                    Toast.makeText(getContext(), "No operation to undo.", Toast.LENGTH_SHORT).show();
                }else{
                    duration -= duration_stack.pop();
                    duration_tv.setText(minToHour(duration));
                }
            }
        });


        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duration = 0;
                duration_stack.clear();
                duration_tv.setText(minToHour(duration));
            }
        });


        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!checkRemainDuration()){return;}

                addNewActivity();

            }
        });




//        Query lastTimeRef = mDatabase.child("users").child(mUserId).child("newestTime");
//        lastTimeRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                String time = dataSnapshot.getValue(String.class);
////                tv_last_time.setText(time);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                String selection = RecordContract.RecordEntry._ID + " = " + id;
                Cursor cursor = mDb.query(RecordContract.RecordEntry.TABLE_NAME,
                        null,
                        selection,
                        null,
                        null,
                        null,
                        null);
                cursor.moveToFirst();
                int theDuration = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DURATION));
                last_time_cal.add(Calendar.MINUTE, -theDuration);
                duration_remain += theDuration;
                tv_duration_remain.setText(minToHour(duration_remain));

                SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String time = dft.format(last_time_cal.getTime());
                tv_last_time.setText(time.toString().substring(11));

                editor.putString("last_time", time);
                editor.apply();

                removeRecord(id);
                mAdapter.lastPosition--;
                mAdapter.swapCursor(getTodayRecords());

            }


        }).attachToRecyclerView(mRecyclerView);
    }


    private void saveTempData(){
        if(duration == 0){
            return;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(calendar.getTime());
        String[] dates = date.split("-");

//        SimpleDateFormat dft = new SimpleDateFormat("HH:mm");
//        String time = dft.format(calendar.getTime());
//        Record record = new Record(curt_activity, duration, date, time, efficient);
        String time = prefs.getString("last_time", null);


        ContentValues cv = new ContentValues();
        cv.put(RecordContract.RecordEntry.COLUMN_TYPE, curt_activity);
        cv.put(RecordContract.RecordEntry.COLUMN_DURATION, duration);
        cv.put(RecordContract.RecordEntry.COLUMN_EFFICIENT, efficient);
        cv.put(RecordContract.RecordEntry.COLUMN_YEAR, Integer.valueOf(dates[0]));
        cv.put(RecordContract.RecordEntry.COLUMN_MONTH, Integer.valueOf(dates[1]));
        cv.put(RecordContract.RecordEntry.COLUMN_DAY, Integer.valueOf(dates[2]));

        if(time == null) {
            cv.put(RecordContract.RecordEntry.COLUMN_STARTTIME, "00:00");
        }else{
            String startTime = time.split(" ")[1];
            cv.put(RecordContract.RecordEntry.COLUMN_STARTTIME, startTime);
        }
        mDb.insert(RecordContract.RecordEntry.TABLE_NAME, null, cv);

        Cursor cursor = getRecords(dates[0], dates[1], dates[2]);
        mAdapter.swapCursor(cursor);

        duration_remain -= duration;
        tv_duration_remain.setText(minToHour(duration_remain));

        mRecyclerView.scrollToPosition(cursor.getCount()-1);

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


    private boolean removeRecord(long id){
        return mDb.delete(RecordContract.RecordEntry.TABLE_NAME,
                RecordContract.RecordEntry._ID + "=" + id, null) > 0;
    }


    // if last record is before today, set the last_time to today 00:00.
    private void initLastTime(){
        String last_time = prefs.getString("last_time", null);
        Calendar now = Calendar.getInstance();
        String pattern = "yyyy-MM-dd HH:mm";

        Cursor cursor = getTodayRecords();
        if (cursor.getCount() == 0){

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String time = df.format(cal.getTime());

            // put 00:00 to last_time
            time += " 00:00";

            editor.putString("last_time", time);
            editor.apply();



            // handle sleep time auto fill
            int wakeupHour = prefs.getInt("wakeupHour", 0);
            int wakeupMinute = prefs.getInt("wakeupMinute", 0);
            if(wakeupHour != 0 || wakeupMinute != 0) {
                String wakeupTime = time2String(prefs.getInt("wakeupHour", 0), prefs.getInt("wakeupMinute", 0));

                curt_activity = "sleep";
                efficient = 4;
                duration = prefs.getInt("wakeupHour", 0) * 60 + prefs.getInt("wakeupMinute", 0);
                addNewActivity();
                time = time.substring(0,11) + wakeupTime;

                editor.putString("last_time", time);
                editor.apply();
            }


            curt_activity = "study";

            try {
                Date date = new SimpleDateFormat(pattern).parse(time);

                long millis = now.getTime().getTime() - date.getTime();
                duration_remain = TimeUnit.MILLISECONDS.toMinutes(millis);
                tv_duration_remain.setText(minToHour(duration_remain));
                last_time_cal.setTime(date);
            }catch (Exception e){
                Log.e("Error in date:", e.getMessage());
            }
            tv_last_time.setText(time.substring(11));
            mAdapter.swapCursor(getTodayRecords());
        }else{
            if(last_time!=null) {
                try {
                    Date date = new SimpleDateFormat(pattern).parse(last_time);

                    if (DateUtils.isToday(date.getTime())) {
                        last_time_cal.setTime(date);

                        long millis = now.getTime().getTime() - date.getTime();
                        duration_remain = TimeUnit.MILLISECONDS.toMinutes(millis);
                        tv_duration_remain.setText(minToHour(duration_remain));
                        tv_last_time.setText(last_time.substring(11));
                    } else {
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String time = df.format(cal.getTime());
                        time += " 00:00";
                        editor.putString("last_time", time);
                        editor.apply();

                        date = new SimpleDateFormat(pattern).parse(time);
                        last_time_cal.setTime(date);

                        long millis = now.getTime().getTime() - date.getTime();
                        duration_remain = TimeUnit.MILLISECONDS.toMinutes(millis);
                        tv_duration_remain.setText(minToHour(duration_remain));
                        tv_last_time.setText(time.substring(11));
                    }
                } catch (Exception e) {
                    Log.e("Error in date:", e.getMessage());
                }
            }else{
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String time = df.format(cal.getTime());
                time += " 00:00";
                editor.putString("last_time", time);
                editor.apply();

                try {
                    Date date = new SimpleDateFormat(pattern).parse(time);

                    long millis = now.getTime().getTime() - date.getTime();
                    duration_remain = TimeUnit.MILLISECONDS.toMinutes(millis);
                    tv_duration_remain.setText(minToHour(duration_remain));
                    last_time_cal.setTime(date);
                }catch (Exception e){
                    Log.e("Error in date:", e.getMessage());
                }
                tv_last_time.setText(time.substring(11));
            }
        }

    }

    private boolean checkRemainDuration(){
        if (duration > duration_remain){
            Toast.makeText(getContext(), "Current duration is larger than remain available duration.", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private void addNewActivity(){
        saveTempData();
        last_time_cal.add(Calendar.MINUTE, duration);
        SimpleDateFormat dft = new SimpleDateFormat("HH:mm");
        String time = dft.format(last_time_cal.getTime());
        tv_last_time.setText(time.toString());
        SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String last_time = pattern.format(last_time_cal.getTime());
        editor.putString("last_time", last_time);
        editor.apply();

        duration = 0;
        duration_tv.setText(minToHour(duration));

        duration_stack.clear();

//        mLayoutManager.scrollToPosition

    }

    private String minToHour(int t){
        String hour = String.valueOf(t / 60);
        String min = String.valueOf(t % 60);
        if(t<60){
            return min+" min";
        }else{
            return hour+" h  "+min+" min";
        }
    }

    private String minToHour(long t){
        String hour = String.valueOf(t / 60);
        String min = String.valueOf(t % 60);
        if(t<60){
            return min+" min";
        }else{
            return hour+" h  "+min+" min";
        }
    }

    private String time2String(int hour, int min){
        String hour_s, min_s;
        if (hour<10){
            hour_s = "0" + String.valueOf(hour);
        }else{
            hour_s = String.valueOf(hour);
        }

        if (min<10){
            min_s = "0" + String.valueOf(min);
        }else{
            min_s = String.valueOf(min);
        }
        return hour_s + ":" + min_s;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }


}
