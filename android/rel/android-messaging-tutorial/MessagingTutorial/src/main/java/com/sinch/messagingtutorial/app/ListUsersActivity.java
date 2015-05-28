package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.messagingtutorial.app.Adapters.RoomAdapter;
import com.sinch.messagingtutorial.app.Feature.MyProgressDialog;
import com.sinch.messagingtutorial.app.Service.MessageService;

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends Activity implements View.OnClickListener {

    private String currentUserId;
    private ArrayAdapter<String> namesArrayAdapter;
    private ArrayList<String> names;
    private ListView usersListView;
    private Button logoutButton;
    private ProgressDialog progressDialog;
    private BroadcastReceiver receiver = null;

    private EditText loginEditText;
    List<ParseObject> roomList = new ArrayList<ParseObject>();
    RoomAdapter cellAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        showSpinner();
        initUI();

    }

    private void initUI() {
        final Button serachButton = (Button) findViewById( R.id.findFriendButton );
        serachButton.setOnClickListener( this );

        loginEditText = (EditText) findViewById( R.id.userNameEditText );
        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(getApplicationContext(), MessageService.class));
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

        cellAdapter = new RoomAdapter( getApplicationContext(), roomList );
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
            openConversation( roomList.get(position) );
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
    private void setConversationsList() {
        if ( null != cellAdapter ) {
            // clear list, if they was initialized before
            cellAdapter.clear();
        }

        MyProgressDialog.start( this );
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("people", ParseUser.getCurrentUser() );

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for ( ParseObject obj : list ) {
                        ParseObject room = obj.getParseObject("room");
                        roomList.add( room );
                    }

                    // init list
                    initListView();
                    MyProgressDialog.stop();
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    //open a conversation with one person
    public void openConversation( /*ArrayList<String> names, int pos, */ParseObject room) {
        /*
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", names.get(pos));
        query.findInBackground(new FindCallback<ParseUser>() {
           public void done(List<ParseUser> user, com.parse.ParseException e) {
               if (e == null) {
                   Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                   intent.putExtra("RECIPIENT_ID", user.get(0).getObjectId());
                   startActivity(intent);
               } else {
                   Toast.makeText(getApplicationContext(),
                       "Error finding that user",
                           Toast.LENGTH_SHORT).show();
               }
           }
        });
        */

        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        MessagingActivity.room = room;
        // intent.putExtra("RECIPIENT_ID", user.get(0).getObjectId());
        startActivity(intent);
    }

    //show a loading spinner while the sinch client starts
    private void showSpinner() {
        MyProgressDialog.start( this );

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                MyProgressDialog.stop( );
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.sinch.messagingtutorial.app.ListUsersActivity"));
    }

    @Override
    public void onClick(View v) {

        switch( v.getId() ) {
            case R.id.findFriendButton:

                final Intent searchPeopleIntent = new Intent( ListUsersActivity.this, SearchPeopleActivity.class );
                searchPeopleIntent.putExtra("login", loginEditText.getText().toString() );
                startActivity( searchPeopleIntent );

                break;
        }

    }

    @Override
    public void onResume() {
        setConversationsList();
        super.onResume();
    }
}


