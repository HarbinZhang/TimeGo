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
    public String date;

    public Record(){

    }

    public Record(String type, int duration, String date, int efficient){
        this.type = type;
        this.duration = duration;
        this.efficient = efficient;
        this.date = date;
    }
}
