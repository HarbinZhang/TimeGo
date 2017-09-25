package com.timego.harbin.timego;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.timego.harbin.timego.MainActivity.mDatabase;
import static com.timego.harbin.timego.MainActivity.mFirebaseUser;
import static com.timego.harbin.timego.MainActivity.mUserId;

public class FeedbackActivity extends AppCompatActivity {

    private ChatView mChatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);


        //User id
        int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        String myName = "Me";

        int yourId = 1;
        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.pika_1);
        String yourName = "TimeGoer";

        final User me = new User(myId, myName, myIcon);
        final User you = new User(yourId, yourName, yourIcon);

        mChatView = (ChatView)findViewById(R.id.chat_view);

        //Set UI parameters if you need
        mChatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.green500));
        mChatView.setLeftBubbleColor(Color.WHITE);
        mChatView.setBackgroundColor(ContextCompat.getColor(this, R.color.blueGray500));
        mChatView.setSendButtonColor(ContextCompat.getColor(this, R.color.cyan500));
        mChatView.setSendIcon(R.drawable.ic_action_send);
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.BLACK);
        mChatView.setUsernameTextColor(Color.WHITE);
        mChatView.setSendTimeTextColor(Color.WHITE);
        mChatView.setDateSeparatorColor(Color.WHITE);
        mChatView.setInputTextHint("new feedback...");
        mChatView.setMessageMarginTop(5);
        mChatView.setMessageMarginBottom(5);


        if(mFirebaseUser!=null){
            Query feedbackQuery = mDatabase.child("users").child(mUserId).child("feedback");
            feedbackQuery.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Message> messages = new ArrayList<Message>();
                    for (DataSnapshot it: dataSnapshot.getChildren()){
                        Message message;
                        String date_s = it.getKey().substring(0,it.getKey().length()-2);
                        String pattern = "yyyy-MM-dd HH:mm:SS";
                        Calendar calendar = Calendar.getInstance();
                        try {
                            Date date = new SimpleDateFormat(pattern).parse(date_s);
                            calendar.setTime(date);
                        }catch (Exception e){

                        }
                        if (it.getKey().split(" ")[2].equals("1")){
                            message = new Message.Builder()
                                    .setUser(me)
                                    .setRightMessage(true)
                                    .setMessageText(it.getValue(String.class))
                                    .hideIcon(true)
                                    .setCreatedAt(calendar)
                                    .build();
                            mChatView.send(message);
                        }else{
                            message = new Message.Builder()
                                    .setUser(you)
                                    .setRightMessage(false)
                                    .setMessageText(it.getValue(String.class))
                                    .setCreatedAt(calendar)
                                    .build();
                            mChatView.receive(message);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
                String time = f.format(cal.getTime());


                //new message
                Message message = new Message.Builder()
                        .setUser(me)
                        .setRightMessage(true)
                        .setMessageText(mChatView.getInputText())
                        .hideIcon(true)
                        .build();
                //Set to chat view
                mChatView.send(message);


                if(mFirebaseUser == null){
                    final Message receivedMessage = new Message.Builder()
                        .setUser(you)
                        .setRightMessage(false)
                        .setMessageText("Please sign in or sign up to talk with me : )")
                        .build();

                    mChatView.receive(receivedMessage);
                }else{
                    mDatabase.child("users").child(mUserId).child("feedback").child(time+" 1").setValue(mChatView.getInputText());
                }

                mChatView.setInputText("");
            }
        });


//        //Click Send Button
//        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //new message
//                Message message = new Message.Builder()
//                        .setUser(me)
//                        .setRightMessage(true)
//                        .setMessageText(mChatView.getInputText())
//                        .hideIcon(true)
//                        .build();
//                //Set to chat view
//                mChatView.send(message);
//                //Reset edit text
//                mChatView.setInputText("");
//
//                //Receive message
//                final Message receivedMessage = new Message.Builder()
//                        .setUser(you)
//                        .setRightMessage(false)
//                        .setMessageText(ChatBot.talk(me.getName(), message.getMessageText()))
//                        .build();
//
//                // This is a demo bot
//                // Return within 3 seconds
//                int sendDelay = (new Random().nextInt(4) + 1) * 1000;
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mChatView.receive(receivedMessage);
//                    }
//                }, sendDelay);
//            }
//
//        });

    }
}
