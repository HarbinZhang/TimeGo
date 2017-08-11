package com.timego.harbin.timego.database;

import android.provider.BaseColumns;

/**
 * Created by Harbin on 8/1/17.
 */



public class RecordContract {
    public static final class RecordEntry implements BaseColumns{
        public static final String TABLE_NAME = "record";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_EFFICIENT = "efficient";
        public static final String COLUMN_YEAR = "year";
        public static final String COLUMN_MONTH = "month";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_STARTTIME = "startTime";
    }
}
