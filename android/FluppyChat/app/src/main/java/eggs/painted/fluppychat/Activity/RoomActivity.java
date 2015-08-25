package eggs.painted.fluppychat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import eggs.painted.fluppychat.Adapters.RoomAdapter;
import eggs.painted.fluppychat.Interface.OpenChat;
import eggs.painted.fluppychat.R;

/**
 * Created by dmytro on 22.08.15.
 */
public class RoomActivity extends Activity implements OpenChat {
    static private final String TAG = "ROOM_ACTIVITY";

    List<ParseObject> roomList = new ArrayList<ParseObject>();
    List<ParseObject> converstationList = new ArrayList<ParseObject>();

    // ui objects
    RecyclerView recList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rooms_layout);

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        loadConversationsList();

        final FloatingActionButton fButton = (FloatingActionButton) findViewById( R.id.floatSearchButton );
        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent searchActivity = new Intent( RoomActivity.this, SearchFrindsActivity.class );
                startActivity( searchActivity );
            }
        });
    }

    /**
     *  Display all room for user
     */
    public void loadConversationsList() {
        Log.d(TAG, "request for the friend list");
        //if ( null != cellAdapter ) cellAdapter.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("people", ParseUser.getCurrentUser());
        query.include("room");

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    // get subscribe list
                    List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
                    if (null == subscribedChannels) subscribedChannels = new ArrayList<String>();
                    Log.d(TAG, String.format("Subscribed list %s", subscribedChannels.toString()));

                    for (ParseObject obj : list) {
                        ParseObject room = obj.getParseObject("room");
                        roomList.add(room);
                        converstationList.add(obj);

                        // if user not subscribet yet
                        if (!subscribedChannels.contains("ROOM_" + room.getObjectId())) {
                            Log.d(TAG, String.format("subscribed added: %s", room.getObjectId()));
                            ParsePush.subscribeInBackground("ROOM_" + room.getObjectId(), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (null == e) {
                                        Log.d(TAG, "subscribe save success");
                                    } else {
                                        Log.e(TAG, "subscribe save error");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }

                    // init list
                    recList.setAdapter( new RoomAdapter(RoomActivity.this, roomList) );
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void openChat(ParseObject room) {
        final Intent chatIntent = new Intent( RoomActivity.this, ChatActivity.class );
        ChatActivity.room = room;
        startActivity( chatIntent );
    }
}
