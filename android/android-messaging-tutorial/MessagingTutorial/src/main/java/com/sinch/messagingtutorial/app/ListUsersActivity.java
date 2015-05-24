package com.sinch.messagingtutorial.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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

        initListView();
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
    }

    /**
     * Open room by clicking
     */
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            openConversation( roomList.get(0) );
        }
    };

    //display clickable a list of all users
    private void setConversationsList() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("people", ParseUser.getCurrentUser() );

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {

                    for ( ParseObject obj : list ) {
                        ParseObject room = obj.getParseObject("room");
                        roomList.add( room );
                        cellAdapter.add( room );
                    }


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
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
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


