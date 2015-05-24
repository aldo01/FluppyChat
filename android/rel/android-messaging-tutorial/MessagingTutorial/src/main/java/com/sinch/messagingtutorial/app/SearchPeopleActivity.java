package com.sinch.messagingtutorial.app;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.messagingtutorial.app.Adapters.ParseUserAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SearchPeopleActivity extends Activity {
    private ParseUserAdapter cellAdapter;
    private List<ParseUser> userList = new ArrayList<ParseUser>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.search_people_layout );

        String login = "lera";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            login = extras.getString("login");
        }

        initListView();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", login);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    for ( ParseUser u : objects ) {
                        cellAdapter.add(u);
                    }
                } else {
                    // Something went wrong.
                    e.printStackTrace();
                }
            }
        });
    }

    private void initListView() {
        ListView myList = (ListView) findViewById(R.id.peopleListViewSearchPeopleActivity);

        cellAdapter = new ParseUserAdapter( getApplicationContext(), userList);
        myList.setAdapter( cellAdapter );
        myList.setOnItemClickListener( clickListener );
    }

    /**
     * Create room with user
     */
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            createRoom( userList.get(position) );
        }
    };

    /**
     * Create new room and add to them 2 people: me and selected user
     *
     * @param user - user for new message
     */
    private void createRoom( ParseUser user ) {
        ParseObject room = new ParseObject( "Room" );
        room.put( "Creator", ParseUser.getCurrentUser() );
        room.saveInBackground();

        ParseObject obj1 = new ParseObject( "PeopleInRoom" );
        obj1.put("people", ParseUser.getCurrentUser());
        obj1.put("room", room);
        obj1.saveInBackground();

        ParseObject obj2 = new ParseObject( "PeopleInRoom" );
        obj2.put("people", user );
        obj2.put("room", room);
        obj2.saveInBackground();

        finish();
    }

}