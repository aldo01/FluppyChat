package eggs.painted.fluppychat.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Adapters.RoomAdapter;
import eggs.painted.fluppychat.Interface.OpenChat;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 22.08.15.
 */
public class RoomActivity extends Activity implements OpenChat {
    static private final String TAG = "ROOM_ACTIVITY";
    private final int PICK_PHOTO_FOR_AVATAR = 1;
    static private final int SEARCH_FRIENDS = 2;
    static private final int OPEN_CHAT = 3;

    List<ParseObject> roomList = new ArrayList<ParseObject>();
    List<ParseObject> converstationList = new ArrayList<ParseObject>();

    // ui objects
    RoomAdapter adapter;
    RecyclerView recList;
    public ActionBarDrawerToggle mDrawerToggle;
    ProgressWheel progressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rooms_layout);

        initUI();
        loadConversationsList();
    }

    /**
     * Initialize UI elements
     */
    private void initUI() {
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel_room_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_menu);
        NavigationView nv = (NavigationView) findViewById( R.id.navigationView );
        View header = getLayoutInflater().inflate(R.layout.drawer_header_layout, null);
        nv.addView(header);

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.roomDrawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // show user photo
        CircleImageView ui = (CircleImageView) findViewById(R.id.userImageNavigationDrawer);
        UserImage.showImage(ParseUser.getCurrentUser(), ui);
        ui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        // init floating button for search friends
        final FloatingActionButton fButton = (FloatingActionButton) findViewById( R.id.floatSearchButton );
        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent searchActivity = new Intent(RoomActivity.this, SearchFrindsActivity.class);
                startActivityForResult(searchActivity, SEARCH_FRIENDS);
            }
        });

        findViewById( R.id.logoutButton ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    /**
     *  Display all room for user
     */
    public void loadConversationsList() {
        progressWheel.setVisibility(View.VISIBLE);
        Log.d(TAG, "request for the friend list");
        //if ( null != cellAdapter ) cellAdapter.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("people", ParseUser.getCurrentUser());
        query.include("room");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    if (null != adapter) {
                        roomList.clear();
                        adapter.notifyItemRangeRemoved(0, roomList.size() - 1);
                    }

                    // get subscribe list
                    List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
                    if (null == subscribedChannels)
                        subscribedChannels = new ArrayList<String>();
                    Log.d(TAG, String.format("Subscribed list %s", subscribedChannels.toString()));

                    if (!subscribedChannels.contains(getString(R.string.new_room) + ParseUser.getCurrentUser().getObjectId())) {
                        ParsePush.subscribeInBackground(getString(R.string.new_room) + ParseUser.getCurrentUser().getObjectId());
                    }

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
                    if (null == adapter) {
                        adapter = new RoomAdapter(RoomActivity.this, roomList, converstationList);
                        recList.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    progressWheel.setVisibility(View.GONE);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void openChat(ParseObject room, ParseObject peopleInRoom ) {
        final Intent chatIntent = new Intent( RoomActivity.this, ChatActivity.class );
        ChatActivity.room = room;
        ChatActivity.peopleInRoom = peopleInRoom;
        startActivityForResult(chatIntent, OPEN_CHAT);
    }

    @Override
    public void acceptRoom() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void declineRoom() {
        adapter.notifyDataSetChanged();
    }


    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            try {
                Log.d( "IMAGE", "start proccess" );
                InputStream is = getContentResolver().openInputStream(data.getData());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] mdata = new byte[16384];

                while ((nRead = is.read(mdata, 0, mdata.length)) != -1) {
                    buffer.write(mdata, 0, nRead);
                }
                buffer.flush();

                if ( buffer.size() > 262144 ) {
                    Toast.makeText( getApplicationContext(), "Max size 256 kb.", Toast.LENGTH_LONG ).show();
                    return;
                }

                final ParseUser currUser = ParseUser.getCurrentUser();
                ParseFile pfile = new ParseFile( currUser.getObjectId() + ".jpg", buffer.toByteArray());
                pfile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d( "IMAGE", "image saved" );
                    }
                });

                currUser.put("profilepic", pfile);
                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        UserImage.removeImage(currUser.getObjectId());
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }

        if ( SEARCH_FRIENDS == requestCode ) {
            if ( RESULT_OK == resultCode ) {
                loadConversationsList();
            }
        }

        if ( OPEN_CHAT == requestCode ) {
            if ( RESULT_OK == resultCode ) {
                loadConversationsList();
            }
        }
    }

    private void logout() {
        // unsubscribe from all rooms
        final ParseInstallation myInstallation = ParseInstallation.getCurrentInstallation();
        List<String> subscribedChannels = myInstallation.getList("channels");
        for ( String chanel : subscribedChannels ) {
            ParsePush.unsubscribeInBackground(chanel);
        }

        ParseUser.logOut();
        finish();
    }

    @Override
    public void onBackPressed() {
        // create new alert dialog
        AlertDialog.Builder mess = new AlertDialog.Builder( RoomActivity.this );
        mess.setMessage("Do you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logout();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        mess.show();
    }
}
