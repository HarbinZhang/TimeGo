package com.timego.harbin.timego.database;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Harbin on 5/29/17.
 */


@IgnoreExtraProperties
public class Record {
    public String type;
    public int duration;
    public int efficient;
    public int year;
    public int month;
    public int day;
    public String startTime;

    public Record(){

    }

    public Record(String type, int duration, int year, int month, int day, String startTime, int efficient){
        this.type = type;
        this.duration = duration;
        this.efficient = efficient;
        this.startTime = startTime;
        this.year = year;
        this.month = month;
        this.day = day;

    }
}
