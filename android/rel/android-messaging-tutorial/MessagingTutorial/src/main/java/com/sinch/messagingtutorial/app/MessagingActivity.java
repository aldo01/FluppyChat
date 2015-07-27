package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.scottyab.aescrypt.AESCrypt;
import com.sinch.messagingtutorial.app.Adapters.MessageAdapter;
import com.sinch.messagingtutorial.app.Feature.MyProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends Activity {
    static public ParseObject room;
    static private MessagingActivity thisActivity = null;
    static private String TAG = "MessagingActivity";

    // ui elements
    private EditText messageBodyField;

    public MessageAdapter messageAdapter;
    private List<ParseUser> companionList = new ArrayList<ParseUser>();
    String myName; // store current user name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity = this;
        setContentView(R.layout.messaging);
        loadCompanions();

        myName = ParseUser.getCurrentUser().getUsername();
        ListView messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    /**
     * Load all users from this room
     */
    private void loadCompanions() {
        MyProgressDialog.start(this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("room", room);
        query.include("people");
        Log.d( "ROOM", room.toString() );
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    for (ParseObject o : list) {
                        ParseUser u = o.getParseUser("people");
                        companionList.add(u);
                        Log.d("MESSAGING", String.format("Add people id:%s", u.getObjectId()));
                    }

                    populateMessageHistory();
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    //get previous messages from parse & display
    private void populateMessageHistory() {
        String [] id = new String[ companionList.size() ];
        int i = 0;
        for ( ParseUser u : companionList ) {
            id[i++] = u.getObjectId();
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereContainedIn("User", companionList);
        query.whereEqualTo("Room", room);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    String currentUserId = ParseUser.getCurrentUser().getObjectId();

                    for (ParseObject obj : messageList) {
                        String name = "";
                        String text = obj.getString("Text");

                        try {
                            ParseUser u = obj.getParseUser("User");
                            name = u.fetchIfNeeded().getString("username");
                        } catch (com.parse.ParseException er) {
                            Log.v("PARSE_ERROR", e.toString());
                            er.printStackTrace();
                        }

                        if (obj.getParseObject("User").getObjectId().equals(currentUserId)) {
                            messageAdapter.addMessage(text, MessageAdapter.DIRECTION_OUTGOING, name);
                        } else {
                            messageAdapter.addMessage(text, MessageAdapter.DIRECTION_INCOMING, name);
                        }

                        MyProgressDialog.stop();
                    }
                } else {
                    e.printStackTrace();
                    Log.e("RESPONSE", "Incorrect response");
                }
            }
        });
    }

    private void sendMessage() {
        String messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }

        // encrypting message with AVC
        String password = "password";
        String encryptedMsg = "";
        try {
            encryptedMsg = AESCrypt.encrypt(password, messageBody);
        }catch (GeneralSecurityException e){
            e.printStackTrace();
            Log.e( "ECNCRYPT_ERROR", "error ocqurence when encrypt message" );
            //handle error
        }

        // send push
        ParsePush push = new ParsePush();
        push.setChannel(room.getObjectId());
        try {
            JSONObject data = new JSONObject();
            data.put( "Alert", encryptedMsg );
            data.put( "Author", ParseUser.getCurrentUser().getObjectId() );
            data.put( "AuthorName", ParseUser.getCurrentUser().getUsername() );
            push.setData( data );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        push.sendInBackground();

        // save message in parse
        ParseObject gameScore = new ParseObject("Message");
        gameScore.put("User", ParseUser.getCurrentUser() );
        gameScore.put("Room", room);
        gameScore.put("Text", encryptedMsg);
        gameScore.saveInBackground();

        messageBodyField.setText("");
    }

    /**
     * Receive message from gcm
     * Call from receiver
     */
    static public void receiveMessage( final String msg,
                                       final String chanel,
                                       final String authorId,
                                       final String authorName ) {
        if ( null != thisActivity ) {
            if (room.getObjectId().equals(chanel)) {
                int direction = authorId.equals( ParseUser.getCurrentUser().getObjectId() ) ? 1 : 0;
                thisActivity.messageAdapter.addMessage( msg, direction, authorName );
            } else {
                Log.d( TAG, String.format("chanel is not the same %s:%s", chanel, room.getObjectId() ) );
            }
        }
    }

    @Override
    protected void onStop() {
        thisActivity = null;

        super.onStop();
    }
}




