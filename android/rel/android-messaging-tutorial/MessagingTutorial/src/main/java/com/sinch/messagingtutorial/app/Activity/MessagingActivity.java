package com.sinch.messagingtutorial.app.Activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.scottyab.aescrypt.AESCrypt;
import com.sinch.messagingtutorial.app.Adapters.MessageAdapter;
import com.sinch.messagingtutorial.app.R;

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

        initUI();
        loadCompanions();
    }

    private void initUI() {
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
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("room", room);
        query.include("people");
        Log.d( "ROOM", room.getObjectId() );
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    for (ParseObject o : list) {
                        ParseUser u = o.getParseUser("people");
                        companionList.add(u);
                        Log.d("MESSAGING", String.format("Add people id:%s", u.getObjectId()));
                    }

                    new DownloadMessageHistory().execute();
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Save message in parse.
     * Sent push notification to another user
     */
    private void sendMessage() {
        // detect message text
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

    /**
     * Download message history from parse and show them to user
     */
    private class DownloadMessageHistory extends AsyncTask {
        private List<String> messageTextList = new ArrayList<String>();
        private List<Integer> messageDirectionList = new ArrayList<Integer>();
        private List<String> messageAuthorList = new ArrayList<String>();

        @Override
        protected Object doInBackground(Object[] objects) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
            query.whereEqualTo("Room", room);
            query.orderByAscending("createdAt");
            try {
                List<ParseObject> messageList = query.find();

                String currentUserId = ParseUser.getCurrentUser().getObjectId();
                // parse data from ParseObject and put them to list
                for (ParseObject obj : messageList) {
                    String name = "";
                    String text = obj.getString("Text");

                    try {
                        ParseUser u = obj.getParseUser("User");
                        name = u.fetchIfNeeded().getString("username");
                    } catch (com.parse.ParseException er) {
                        Log.v("PARSE_ERROR", "an error ocqurence");
                        er.printStackTrace();
                    }
                    Log.d("message", "add messages");
                    messageTextList.add(text);
                    messageAuthorList.add(name);
                    messageDirectionList.add(obj.getParseObject("User").getObjectId().equals(currentUserId) ? MessageAdapter.DIRECTION_OUTGOING : MessageAdapter.DIRECTION_INCOMING);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            // show messages
            for (int i = 0; i < messageTextList.size(); i++) {
                messageAdapter.addMessage(messageTextList.get(i), messageDirectionList.get(i), messageAuthorList.get(i));
            }
        }
    }

    @Override
    protected void onStop() {
        thisActivity = null;
        super.onStop();
    }
}




