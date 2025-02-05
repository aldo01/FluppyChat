package eggs.painted.fluppychat.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Activity.RoomActivity;
import eggs.painted.fluppychat.Interface.OpenChat;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 22.08.15.
 *
 * Adapter for showing rooms
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private final int USER_IMAGE_SIZE = 96;
    private Context context;
    private static List<ParseObject> roomList, peopleInRoomList, peopleInRoomOtherUserList;
    private OpenChat callback;
    private SharedPreferences sharedPref;

    public RoomAdapter( RoomActivity activity, List<ParseObject> roomList, List<ParseObject> peopleInRoomList) {
        this.roomList = roomList;
        this.peopleInRoomList = peopleInRoomList;
        this.context = activity.getApplicationContext();
        this.callback = (OpenChat) activity;
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        loadOtherPeople();
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    @Override
    public void onBindViewHolder(final RoomViewHolder contactViewHolder, int i) {
        ParseObject r = roomList.get(i);
        ParseObject peopleInRoom = peopleInRoomList.get(i);

        contactViewHolder.putData( r, peopleInRoom );
        Log.d( "ROOM_ADAPTER", String.format( "ROOM ID: %s %s", r.getObjectId(), String.valueOf(peopleInRoom.getBoolean("confirm")) ) );
        contactViewHolder.containerLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, USER_IMAGE_SIZE));
        if ( peopleInRoom.getBoolean("confirm") ) {
            contactViewHolder.initUI(r);
            int messageCount = sharedPref.getInt( context.getString(R.string.notificationCountKey) +
                    context.getString(R.string.chanelPrefix) + r.getObjectId(), 0 );
            Log.d( "ROOM_ADAPTER", String.format( "message count %d for room %s", messageCount, context.getString(R.string.notificationCountKey) + r.getObjectId()) );
            if ( 0 != messageCount ) {
                Log.d( "ROOM_ADAPTER", "show incommig message count" );
                contactViewHolder.incommigCountMessageTV.setText( String.valueOf(messageCount) );
                contactViewHolder.incommigCountMessageTV.setVisibility(View.VISIBLE);
            }
        } else {
            contactViewHolder.initUI(peopleInRoom);
        }

        List<ParseObject> subList = new ArrayList<>();
        for ( ParseObject o : peopleInRoomOtherUserList ) {
            if ( o.getParseObject("room").getObjectId().equals( r.getObjectId() ) ) {
                subList.add(o);
            }
        }

        contactViewHolder.containerLayout.removeAllViews();
        Log.d("ROOM", String.format("receive people list, count %d", subList.size()) );
        int j = 1;
        for ( ParseObject o : subList ) {
            CircleImageView img = new CircleImageView(context);
            img.setLayoutParams(new ViewGroup.LayoutParams(USER_IMAGE_SIZE, USER_IMAGE_SIZE));
            UserImage.showImage(o.getParseUser("people"), img);
            contactViewHolder.containerLayout.addView(img);
            Log.d( "J = ", String.valueOf(j) );
            j++;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ParseObject o = peopleInRoomList.get(position);
        if ( o.getBoolean("confirm") ) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if ( 0 == i ) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.room_cell_layout, viewGroup, false);

            RoomViewHolder holder = new RoomViewHolderConfirmed(callback, itemView);
            Log.d("ROOM_NUM", String.valueOf(i));
            return holder;
        } else {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.room_not_confirmed_cell, viewGroup, false);

            RoomViewHolder holder = new RoomViewHolderNotConfirmed(callback, itemView);
            Log.d("ROOM_NUM", String.valueOf(i));
            return holder;
        }
    }

    public void reloadData() {
        loadOtherPeople();
        this.notifyDataSetChanged();
    }

    private void loadOtherPeople() {
        // get other people with him talk user
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereContainedIn("room", roomList);
        query.include("people");
        query.include("room");
        try {
            peopleInRoomOtherUserList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Default holder class.
     * Contain union property for confirm and not confiem class
     */
    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout containerLayout;
        protected Button confirmButton, declineButton;
        protected OpenChat callback;
        protected View v;
        protected ParseObject room, peopleInRoom;
        protected TextView incommigCountMessageTV;

        public RoomViewHolder( final OpenChat callback, View v ) {
            super(v);
            this.v = v;
            this.callback = callback;
            containerLayout =  (LinearLayout) v.findViewById(R.id.imageContainerLayout);
        }

        public void initUI( final ParseObject room ) {

        }

        public void putData( final ParseObject roomO, final ParseObject peopleInroomO ) {
            this.room = roomO;
            this.peopleInRoom = peopleInroomO;
        }
    }

    /**
     * Confirmed view holder.
     * Init parent propreties from extended class
     */
    public static class RoomViewHolderNotConfirmed extends RoomViewHolder {

        public RoomViewHolderNotConfirmed( final OpenChat callback, View v ) {
            super(callback, v);
            confirmButton = (Button) v.findViewById(R.id.confirmButton);
            declineButton = (Button) v.findViewById(R.id.declineButton);
        }

        public void initUI( final ParseObject peopleInRoom ) {
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d( "CONFIRM", "confirm" );
                    peopleInRoom.put("confirm", true);
                    peopleInRoom.saveInBackground();
                    callback.acceptRoom();
                }
            });

            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d( "CONFIRM", "confirm" );
                    roomList.remove(room);
                    peopleInRoomList.remove(peopleInRoom);
                    callback.declineRoom();
                    peopleInRoom.deleteInBackground();
                }
            });
        }
    }

    /**
     * Not confirmed view holder.
     * Init parent propreties from extended class
     */
    public static class RoomViewHolderConfirmed extends RoomViewHolder {
        public RoomViewHolderConfirmed( final OpenChat callback, View v ) {
            super(callback, v);
        }

        public void initUI( final ParseObject room ) {
            Log.d("ROOM_ID", room.getObjectId());
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ROOM_ID", room.getObjectId());
                    incommigCountMessageTV.setVisibility(View.GONE);
                    callback.openChat(room, peopleInRoom);
                }
            });

            incommigCountMessageTV = (TextView) v.findViewById(R.id.incommingMessageCountTV);
        }
    }
}

