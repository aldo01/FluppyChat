package com.sinch.messagingtutorial.app.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sinch.messagingtutorial.app.Adapters.RoomAdapter;
import com.sinch.messagingtutorial.app.R;

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends Activity implements View.OnClickListener {
    static private final String TAG = "LIST_USER_ACTIVITY";

    private EditText loginEditText;
    List<ParseObject> roomList = new ArrayList<ParseObject>();
    List<ParseObject> converstationList = new ArrayList<ParseObject>();
    RoomAdapter cellAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( null == cellAdapter ) {
            loadConversationsList();
        }
    }

    private void initUI() {
        final Button serachButton = (Button) findViewById( R.id.findFriendButton );
        serachButton.setOnClickListener( this );

        loginEditText = (EditText) findViewById( R.id.userNameEditText );
        final Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Init list view.
     * Show all user rooms
     */
    private void initListView() {
        ListView myList = (ListView) findViewById(R.id.usersListView);

        cellAdapter = new RoomAdapter( getApplicationContext(), this, roomList );
        myList.setAdapter( cellAdapter );
        myList.setOnItemClickListener( clickListener );
        myList.setOnItemLongClickListener( longClickListener );
    }

    /**
     * Open room by clicking
     */
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            openConversation( converstationList.get(position) );
        }
    };

    /**
     * Show dialog for additing new people to the room
     */
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       final int pos, long id) {

            // create new alert dialog
            AlertDialog.Builder mess = new AlertDialog.Builder( ListUsersActivity.this );

            // Get the layout inflater
            LayoutInflater inflater = getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View dialogView = inflater.inflate(R.layout.add_friend_dialog, null);
            mess.setView( dialogView );

            // edit text where user enter count of product
            final EditText loginEditText = (EditText) dialogView.findViewById( R.id.userNameEditTextAddDialog );

            mess.setMessage("Enter name of new people")
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        private void addToTheRoom( ParseUser user ) {
                            ParseObject room = roomList.get(pos);

                            // put user to the new room
                            ParseObject obj1 = new ParseObject( "PeopleInRoom" );
                            obj1.put("people", user);
                            obj1.put("room", room);
                            obj1.saveInBackground();

                            // change room name
                            String name = room.getString("Name");
                            name += String.format(", %s", user.getString("username"));
                            room.put("Name", name);
                            room.saveInBackground();
                        }

                        public void onClick(DialogInterface dialog, int id) {
                            String login =  loginEditText.getText().toString();

                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("username", login);
                            query.findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> objects, ParseException e) {
                                    if (e == null) {
                                        for ( ParseUser u : objects ) {
                                            addToTheRoom( u );
                                        }
                                    } else {
                                        // Something went wrong.
                                        e.printStackTrace();
                                    }
                                }
                            });

                            Toast.makeText( getApplicationContext(), "User added", Toast.LENGTH_SHORT ).show();
                        }
                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            mess.show();

            return true;
        }
    };


    /**
     *  Display all room for user
     */
    public void loadConversationsList() {
        Log.d( TAG, "request for the friend list" );
        if ( null != cellAdapter ) cellAdapter.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("people", ParseUser.getCurrentUser());
        query.include("room");

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    // get subscribe list
                    List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
                    if ( null == subscribedChannels ) subscribedChannels = new ArrayList<String>();
                    Log.d( TAG, String.format( "Subscribed list %s", subscribedChannels.toString() ) );

                    for (ParseObject obj : list) {
                        roomList.add(obj);
                        ParseObject room = obj.getParseObject("room");
                        converstationList.add( room );

                        // if user not subscribet yet
                        if ( !subscribedChannels.contains( "ROOM_" + room.getObjectId() ) ) {
                            Log.d( TAG, String.format("subscribed added: %s", room.getObjectId()) );
                            ParsePush.subscribeInBackground( "ROOM_" + room.getObjectId(), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if ( null == e ) {
                                        Log.d( TAG, "subscribe save success" );
                                    } else {
                                        Log.e( TAG, "subscribe save error" );
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }

                    // init list
                    initListView();
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Open converstation for room
     *
     * @param room - room that will be opened
     */
    public void openConversation( ParseObject room) {
        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        MessagingActivity.room = room;
        startActivity(intent);
    }

    private final int OPEN_SEARCH_FRIEND_ACTIVITY = 1;
    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.findFriendButton:
                final Intent searchPeopleIntent = new Intent( ListUsersActivity.this, SearchPeopleActivity.class );
                searchPeopleIntent.putExtra("login", loginEditText.getText().toString());
                startActivityForResult(searchPeopleIntent, OPEN_SEARCH_FRIEND_ACTIVITY);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( OPEN_SEARCH_FRIEND_ACTIVITY == requestCode ) {
            if ( RESULT_OK == resultCode ) {
                loadConversationsList();
            }
        }
    }
}


