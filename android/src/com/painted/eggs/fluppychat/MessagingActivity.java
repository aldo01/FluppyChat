package com.painted.eggs.fluppychat;

import java.util.List;

import com.painted.eggs.fluppychat.sinch.MessageService;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MessagingActivity extends Activity {
	private String recipientId;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener  = new MyMessageClientListener();;
    private MessageAdapter messageAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        
        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
        //get recipientId from the intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");
        currentUserId = ParseUser.getCurrentUser().getObjectId();
        messageBodyField = (EditText) findViewById(R.id.messageBodyField);
        //listen for a click on the send button
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	messageBody = messageBodyField.getText().toString();
            	if (messageBody.isEmpty()) {
            	    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
            	    return;
            	}
            	messageService.sendMessage(recipientId, messageBody);
            	messageBodyField.setText("");
            }
        });
        
        ListView messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
    }
    
    //unbind the service when the activity is destroyed
    @Override
    public void onDestroy() {
        unbindService(serviceConnection);
        messageService.removeMessageClientListener(messageClientListener);
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
        //Notify the user if their message failed to send
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            Toast.makeText(MessagingActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            //Display an incoming message      
        	if (message.getSenderId().equals(recipientId)) {
        	    WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
        	    messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
        	}
         }
        
        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            //Display the message that was just sent
            //Later, I'll show you how to store the
            //message in Parse, so you can retrieve and
            //display them every time the conversation is opened
        	
        	WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
        	messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
        }
        //Do you want to notify your user when the message is delivered?
        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}
        //Don't worry about this right now
        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}
