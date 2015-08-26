package eggs.painted.fluppychat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import eggs.painted.fluppychat.Adapters.FriendCellAdapter;
import eggs.painted.fluppychat.Interface.CreateRoom;
import eggs.painted.fluppychat.R;

/**
 * Created by dmytro on 23.08.15.
 */
public class SearchFrindsActivity extends Activity implements CreateRoom {
    FriendCellAdapter cellAdapter;

    // ui objects
    RecyclerView recList;
    EditText loginET;
    ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_friend_activity);

        initUI();
    }

    private void initUI() {
        recList = (RecyclerView) findViewById(R.id.friendsList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        loginET = (EditText) findViewById(R.id.loginSerchET);
        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        final Button findButton = (Button) findViewById(R.id.findFriendButton);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPeople();
            }
        });
    }

    private void searchPeople() {
        wheel.setVisibility(View.VISIBLE);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", loginET.getText().toString() );
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    cellAdapter = new FriendCellAdapter( SearchFrindsActivity.this, objects );
                    recList.setAdapter(cellAdapter);
                } else {
                    // Something went wrong.
                    e.printStackTrace();
                }

                wheel.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Create new room and add to them 2 people: me and selected user
     *
     * @param user - user for new message
     */
    @Override
    public void createRoom(ParseUser user) {
        try {
            wheel.setVisibility(View.VISIBLE);
            ParseObject room = new ParseObject( "Room" );
            room.put( "Creator", ParseUser.getCurrentUser() );
            room.put( "Name", String.format("%s, %s", ParseUser.getCurrentUser().getUsername(), user.getUsername()) );

            room.save();

            ParsePush.subscribeInBackground("ROOM_" + room.getObjectId());

            ParseObject obj1 = new ParseObject( "PeopleInRoom" );
            obj1.put("people", ParseUser.getCurrentUser());
            obj1.put("confirm", true);
            obj1.put("room", room);
            obj1.saveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    // push success result
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", 1);
                    setResult(RESULT_OK, returnIntent);
                    wheel.setVisibility(View.GONE);
                    finish();
                }
            });

            ParseObject obj2 = new ParseObject( "PeopleInRoom" );
            obj2.put("people", user );
            obj2.put("confirm", false);
            obj2.put("room", room);
            obj2.saveInBackground();

            // send push with invite about new room
            ParsePush push = new ParsePush();
            push.setChannel(getString(R.string.new_room) + user.getObjectId());
            push.setMessage( getString(R.string.roomInvite) );
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }
}
