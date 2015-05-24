package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessagingActivity extends Activity {
    static public ParseObject room;

    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private MessageAdapter messageAdapter;
    private ListView messagesList;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageClientListener messageClientListener = new MyMessageClientListener();

    private List<ParseUser> companionList = new ArrayList<ParseUser>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        loadCompanions();

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        messagesList = (ListView) findViewById(R.id.listMessages);
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
        if ( null == room ) {
            Log.d("MESSAGING", "room is null");
        }
        Log.d( "MESSAGING", "room not null" );

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("room", room );
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    for ( ParseObject o : list) {
                        ParseUser u = o.getParseUser("people");
                        companionList.add( u );
                        Log.d( "MESSAGING", String.format( "Add people id:%s", u.getObjectId() ));
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
        query.whereContainedIn("User", companionList );
        query.whereEqualTo("Room", room );
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    String currentUserId = ParseUser.getCurrentUser().getObjectId();
                    Log.d( "RESPONSE", String.format( "Get message history: %d", messageList.size() ) );

                    for ( ParseObject obj : messageList ) {
                        String name = "";
                        String text = obj.getString("Text");

                        try {
                            ParseUser u = obj.getParseUser( "User" );
                            name = u.fetchIfNeeded().getString("username");
                        } catch (com.parse.ParseException er) {
                            Log.v("PARSE_ERROR", e.toString());
                            er.printStackTrace();
                        }

                        Log.d( "MESSAGE", String.format( "MESSAGE: %s by %s", text, name ) );
                        WritableMessage message = new WritableMessage( obj.getObjectId(), text );
                        if (  obj.getParseObject("User").getObjectId().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, name);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING, name);
                        }
                    }
                } else {
                    e.printStackTrace();
                    Log.e( "RESPONSE", "Incorrect response" );
                }
            }
        });
    }

    private void sendMessage() {
        messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }

        for ( ParseUser u : companionList ) {
            Log.d("MESSAGE", String.format("%s to %s", messageBody, u.getObjectId()));
            messageService.sendMessage(u.getObjectId(), messageBody);
        }

        // save message in parse
        ParseObject gameScore = new ParseObject("Message");
        gameScore.put("User", ParseUser.getCurrentUser() );
        gameScore.put("Room", room );
        gameScore.put("Text", messageBody );
        gameScore.saveInBackground();

        messageBodyField.setText("");
    }

    @Override
    public void onDestroy() {
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            Toast.makeText(MessagingActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            for ( ParseUser u : companionList ) {
                if (message.getSenderId().equals(u.getObjectId())) {
                    String name = "";
                    try {
                        name = u.fetchIfNeeded().getString("username");
                    } catch (com.parse.ParseException e) {
                        Log.v("PARSE_ERROR", e.toString());
                        e.printStackTrace();
                    }

                    WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                    messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING, name );
                }
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {

            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //only add message to parse database if it doesn't already exist there
           /*
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                    if (e == null) {
                        if (messageList.size() == 0) {
                            ParseObject parseMessage = new ParseObject("ParseMessage");
                            parseMessage.put("senderId", currentUserId);
                            parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING, ParseUser.getCurrentUser().getUsername() );
                        }
                    }
                }
            });
            */
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}




