package com.timego.harbin.timego;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timego.harbin.timego.database.RecordContract;
import com.timego.harbin.timego.database.RecordDbHelper;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private Context context;

    private Cursor cursor;

    private SQLiteDatabase mDb;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;



    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cv_container;

        public TextView tv_type;
        public TextView tv_duration;
        public TextView tv_efficient;


        public ViewHolder(View itemView) {
            super(itemView);
            cv_container = (CardView) itemView.findViewById(R.id.cv_record);
            tv_type = (TextView) itemView.findViewById(R.id.tv_record_type);
            tv_duration = (TextView) itemView.findViewById(R.id.tv_record_duration);
            tv_efficient = (TextView) itemView.findViewById(R.id.tv_record_efficient);

        }
    }

    public RecordAdapter(){

    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public RecordAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;

        RecordDbHelper dbHelper = new RecordDbHelper(context);
        mDb = dbHelper.getWritableDatabase();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();

        editor.putString("scanned_loc_code", null);
        editor.apply();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_record_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);



        return vh;
    }



    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if(!cursor.moveToPosition(position)){
            return;
        }

        final long sqlId = cursor.getLong(cursor.getColumnIndex(RecordContract.RecordEntry._ID));
        final String type = cursor.getString(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_TYPE));
        final int duration = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_DURATION));
        final int efficient = cursor.getInt(cursor.getColumnIndex(RecordContract.RecordEntry.COLUMN_EFFICIENT));


        holder.itemView.setTag(sqlId);
        holder.tv_type.setText(type);
        holder.tv_duration.setText(minToHour(duration));
        holder.tv_efficient.setText(String.valueOf(efficient));

        if(type.equals("study")){
            holder.cv_container.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.study));
        }else if(type.equals("entertain")){
            holder.cv_container.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.entertain));
        }else if(type.equals("sleep")){
            holder.cv_container.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.sleep));
        }else if(type.equals("exercise")){
            holder.cv_container.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.exercise));
        }else if(type.equals("trash")){
            holder.cv_container.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.trash));
        }


//        if ( == 1){
//            holder.cv_container.setCardBackgroundColor(Color.GRAY);
//        }

//        holder.cv_container.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Intent intent = new Intent(context, RecordActivity.class);
//
//
//                final String loc_code = prefs.getString("scanned_loc_code",null);
//                final String loc_name = prefs.getString("curt_loc_name", null);
//
//
//
//                if(loc_name == null){
//                    intent.putExtra("loc_name", "");
//                }else{
////                    detail = new CheckInDetail(id, quantity, unit, loc_code, loc_name, certificate);
//                    intent.putExtra("loc_name", loc_name);
//                }
//
//                intent.putExtra("id", id);
//                intent.putExtra("quantity", quantity);
//                intent.putExtra("unit", unit);
//                intent.putExtra("loc_code", loc_code);
//                intent.putExtra("certificate", certificate);
//                intent.putExtra("name", name);
//                intent.putExtra("sqlId", sqlId);
//
//
//
//                if (loc_code == null){
//                    AlertDialog.Builder builder;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
//                    } else {
//                        builder = new AlertDialog.Builder(context);
//                    }
//                    builder.setTitle("货架未锁定")
//                            .setMessage("确定继续吗？")
//                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    context.startActivity(intent);
//                                }
//                            })
//                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // do nothing
//                                    return;
//                                }
//                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .show();
//                }else{
//                    context.startActivity(intent);
//                }
//
//
//
//            }
//        });
//


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        if(cursor != null) cursor.close();
        cursor = newCursor;
        if(newCursor != null){
            this.notifyDataSetChanged();
        }
    }

    private Cursor getAllRecord(){
        return mDb.query(
                RecordContract.RecordEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
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
}