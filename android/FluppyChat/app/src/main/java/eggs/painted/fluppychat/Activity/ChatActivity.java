package eggs.painted.fluppychat.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import eggs.painted.fluppychat.Adapters.MessageAdapter;
import eggs.painted.fluppychat.Model.Message;
import eggs.painted.fluppychat.R;

/**
 * Created by dmytro on 23.08.15.
 */
public class ChatActivity extends Activity {
    static public ParseObject room;
    static private ChatActivity thisActivity = null;
    static private String TAG = "MessagingActivity";

    String myName; // store current user name
    MessageAdapter adapter;
    List<Message> messageList;

    // ui elements
    RecyclerView recList;
    LinearLayoutManager llm;
    EditText messageBodyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        Log.d("ROOM_ID", room.getObjectId());

        // init singeltoon
        ChatActivity.thisActivity = ChatActivity.this;

        initUI();
        obtainMessageHistory();
    }

    private void initUI() {
        myName = ParseUser.getCurrentUser().getUsername();
        messageList = new ArrayList<>();

        recList = (RecyclerView) findViewById(R.id.cardMessageList);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        messageBodyField = (EditText) findViewById(R.id.messageTextET);
        findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void obtainMessageHistory() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("Room", room);
        query.orderByDescending("createdAt");
        query.include("User");
        query.setLimit(50);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if ( null == e ) {

                    Log.d("MESSAGE_INFO", String.format( "done: %d", list.size()) );
                    // for (ParseObject o : list) {
                    for ( int i = list.size() - 1; i >= 0; i-- ) {
                        ParseObject o = list.get(i);
                        ParseUser u = o.getParseUser("User");
                        if ( null != u ) {
                            Log.d( "USER", u.getObjectId() );
                        }

                        Message m = new Message();
                        m.date = o.getUpdatedAt();
                        m.text = o.getString("Text");
                        m.user = o.getParseUser("User");
                        messageList.add(m);
                    }

                    adapter = new MessageAdapter( getApplicationContext(), messageList );
                    recList.setAdapter( adapter );
                    llm.scrollToPosition( messageList.size() - 1 );
                    adapter.showAnimation();
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
            Log.e("ECNCRYPT_ERROR", "error ocqurence when encrypt message");
            //handle error
        }

        // send push
        ParsePush push = new ParsePush();
        push.setChannel( getString(R.string.chanelPrefix) + room.getObjectId() );
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
            if ( (thisActivity.getString(R.string.chanelPrefix) + room.getObjectId()).equals(chanel)) {
                Log.d( TAG, "Show received message" );
                Message m = new Message();
                m.text = msg;
                m.userId = authorId;
                m.userName = authorName;
                thisActivity.messageList.add(m);
                thisActivity.adapter.notifyDataSetChanged();
                thisActivity.llm.scrollToPosition(thisActivity.messageList.size() - 1 );
            } else {
                Log.d( TAG, String.format("chanel is not the same %s:%s", chanel, room.getObjectId() ) );
            }
        }

    }
}
