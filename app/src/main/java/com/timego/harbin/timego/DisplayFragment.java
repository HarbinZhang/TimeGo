package com.timego.harbin.timego;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.timego.harbin.timego.database.RecordContract;
import com.timego.harbin.timego.database.RecordDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DisplayFragment extends Fragment {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    private ListView today_lv;

    private ArrayList<String> mType = new ArrayList<>();

    private Map<String,Integer> todayTimeSum = new HashMap<>();

    private PieChart mChart;
    private Typeface mTfLight;
    private Typeface mTfRegular;

    protected String[] mParties = new String[] {
            "study", "entertain", "sleep", "exercise", "trash"
    };


    private SQLiteDatabase mDb;

    public DisplayFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_display, container, false);
//        mFirebaseAuth = FirebaseAuth.getInstance();
//        mFirebaseUser = mFirebaseAuth.getCurrentUser();
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mUserId = mFirebaseUser.getUid();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(calendar.getTime());


        RecordDbHelper dbHelper = new RecordDbHelper(getContext());
        mDb = dbHelper.getReadableDatabase();

        mapInit();

        Cursor cursor = getTodayRecords();
        if (cursor != null){
            if(cursor.moveToFirst()){
                do{
                    int curtDuration = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DURATION));
                    String type = cursor.getString(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_TYPE));
                    todayTimeSum.put(type, curtDuration + todayTimeSum.get(type));
                }while(cursor.moveToNext());
            }
        }


//        final Query todayList = mDatabase.child("users").child(mUserId).child("records").orderByChild("date").equalTo(date);
//        todayList.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                mapInit();
//                for(DataSnapshot ds:dataSnapshot.getChildren()){
//                    Record record = ds.getValue(Record.class);
//                    int curtDuration = todayTimeSum.get(record.type) + record.duration;
//                    todayTimeSum.put(record.type, curtDuration);
//                }
////                arrayAdapter.add(todayTimeSum.get("study").toString());
//                if(isAdded()){
//                    setData();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


        // pie chart
        mTfLight = Typeface.createFromAsset(getResources().getAssets(), "OpenSans-Light.ttf");
        mTfRegular = Typeface.createFromAsset(getResources().getAssets(), "OpenSans-Regular.ttf");
        mChart = (PieChart) view.findViewById(R.id.display_pie);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterTextTypeface(mTfRegular);
        mChart.setCenterText(generateCenterSpannableText());

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
//        mChart.setOnChartValueSelectedListener();

        setData();

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);



        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(ContextCompat.getColor(getContext(), R.color.pieData));
        mChart.setEntryLabelTextSize(12f);


        return view;
    }

    private void mapInit(){
        todayTimeSum.clear();
        todayTimeSum.put("study",0);
        todayTimeSum.put("entertain",0);
        todayTimeSum.put("sleep",0);
        todayTimeSum.put("exercise",0);
        todayTimeSum.put("trash",0);

    }

    private void setData() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < todayTimeSum.size() ; i++) {
            int duration = todayTimeSum.get(mParties[i]);
            if(duration == 0){
                continue;
            }
            entries.add(new PieEntry((float) (todayTimeSum.get(mParties[i])),
                    mParties[i % mParties.length],
                    getResources().getDrawable(R.drawable.star)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Election Results");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(ContextCompat.getColor(getContext(), R.color.pieData));
        data.setValueTypeface(mTfRegular);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

//    private SpannableString generateCenterSpannableText() {
//
//        SpannableString s = new SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda");
//        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
//        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
//        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
//        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
//        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
//        return s;
//    }

    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString("Today\n TimeGo");
        s.setSpan(new RelativeSizeSpan(2f), 0, 8, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 8, s.length(), 0);
        return s;
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



}
