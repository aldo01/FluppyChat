package eggs.painted.fluppychat.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Adapters.MessageAdapter;
import eggs.painted.fluppychat.Adapters.UserHereAdapter;
import eggs.painted.fluppychat.Interface.AddPeopleToRoom;
import eggs.painted.fluppychat.Model.Message;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 23.08.15.
 */
public class ChatActivity extends Activity implements AddPeopleToRoom {
    static public ParseObject room, peopleInRoom;
    static private ChatActivity thisActivity = null;
    static private String TAG = "MessagingActivity";

    String myName; // store current user name
    MessageAdapter adapter;
    List<Message> messageList;
    public ActionBarDrawerToggle mDrawerToggle;

    // ui elements
    RecyclerView recList;
    LinearLayoutManager llm;
    EditText messageBodyField;
    View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        Log.d("ROOM_ID", room.getObjectId());

        // init singeltoon
        ChatActivity.thisActivity = ChatActivity.this;

        initUI();
        obtainMessageHistory();
        obtainUserList();
    }

    private void initUI() {
        myName = ParseUser.getCurrentUser().getUsername();
        messageList = new ArrayList<>();

        recList = (RecyclerView) findViewById(R.id.cardMessageList);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        NavigationView nv = (NavigationView) findViewById( R.id.messagingNavigationView );
        headerView = getLayoutInflater().inflate(R.layout.drawer_header_layout, null);
        nv.addView(headerView);
        CircleImageView ui = (CircleImageView) headerView.findViewById(R.id.userImageNavigationDrawer);
        UserImage.showImage(ParseUser.getCurrentUser(), ui);

        messageBodyField = (EditText) findViewById(R.id.messageTextET);
        findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != peopleInRoom) {
                    peopleInRoom.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }
                    });

                }
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
                if (null == e) {

                    Log.d("MESSAGE_INFO", String.format("done: %d", list.size()));
                    // for (ParseObject o : list) {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        ParseObject o = list.get(i);
                        ParseUser u = o.getParseUser("User");

                        Message m = new Message();
                        m.date = o.getUpdatedAt();
                        m.text = o.getString("Text");
                        m.user = o.getParseUser("User");
                        messageList.add(m);
                    }

                    adapter = new MessageAdapter(getApplicationContext(), messageList);
                    recList.setAdapter(adapter);
                    llm.scrollToPosition(messageList.size() - 1);
                }
            }
        });
    }

    private void obtainUserList() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("room", room);
        query.whereEqualTo("confirm", true);
        query.include("people");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if ( null == e ) {
                    List<ParseUser> listUser = new ArrayList<ParseUser>();
                    Log.d( "USER_LIST", String.format("User count: %d %s", list.size(), list.toString()) );
                    for ( ParseObject o : list ) {
                    //for ( int i = 0; i < list.size(); i++ ) {
                    //    ParseObject o = list.get(i);

                        ParseUser u = o.getParseUser("people");
                        listUser.add(u);
                    }

                    // show user list in listview from navigation drawer
                    final ListView userLV = (ListView) headerView.findViewById(R.id.userHereList);
                    userLV.setAdapter( new UserHereAdapter( ChatActivity.this, listUser) );
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
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if ( null == e ) {
                    Log.d( "PUSH", "push sent" );
                } else {
                    Log.e( "PUSH", "error" );
                    e.printStackTrace();
                }
            }
        });

        // add message to the list
        final Message m = new Message();
        m.text = encryptedMsg;
        m.userId = ParseUser.getCurrentUser().getObjectId();
        m.userName = myName;
        messageList.add(m);
        showMessage();

        // save message in parse
        ParseObject gameScore = new ParseObject("Message");
        gameScore.put("User", ParseUser.getCurrentUser() );
        gameScore.put("Room", room);
        gameScore.put("Text", encryptedMsg);
        gameScore.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (null != e) {
                    // notify that message not sent
                    m.saved = false;
                    adapter.notifyDataSetChanged();
                }
            }
        });

        messageBodyField.setText("");
        Log.d("MESSAGE", "message sent");
    }

    public void showMessage() {
        adapter.showAnimation();
        adapter.notifyDataSetChanged();
        llm.scrollToPosition(thisActivity.messageList.size() - 1);
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
                // if message not from me
                if ( !authorId.equals( ParseUser.getCurrentUser().getObjectId() ) ) {
                    Log.d( TAG, "Show received message" );
                    Message m = new Message();
                    m.text = msg;
                    m.userId = authorId;
                    m.userName = authorName;
                    thisActivity.messageList.add(m);
                    thisActivity.showMessage();
                }
            } else {
                Log.d( TAG, String.format("chanel is not the same %s:%s", chanel, room.getObjectId() ) );
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void addPeople() {
        // create new alert dialog
        AlertDialog.Builder mess = new AlertDialog.Builder( ChatActivity.this );

        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogView = inflater.inflate(R.layout.add_people_dialog, null);
        mess.setView(dialogView);

        final EditText usernameET = (EditText) dialogView.findViewById(R.id.usernameForSearchET);
        mess.setMessage("Add People")
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String username = usernameET.getText().toString();
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username", username);
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> list, ParseException e) {
                            if ( null == e ) {
                                if ( 0 != list.size() ) {
                                    ParseUser user = list.get(0);

                                    ParseObject obj = new ParseObject("PeopleInRoom");
                                    obj.put("people", user);
                                    obj.put("confirm", false);
                                    obj.put("room", room);
                                    obj.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Toast.makeText(getApplicationContext(), "Invitation sent", Toast.LENGTH_LONG).show();
                                        }
                                    });

                                    // send push with invite about new room
                                    ParsePush push = new ParsePush();
                                    push.setChannel(getString(R.string.new_room) + user.getObjectId());
                                    push.setMessage(getString(R.string.roomInvite));
                                } else {
                                    Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            })

            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
        mess.show();
    }
}
