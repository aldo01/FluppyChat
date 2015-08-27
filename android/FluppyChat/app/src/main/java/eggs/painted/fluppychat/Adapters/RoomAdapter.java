package eggs.painted.fluppychat.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Activity.RoomActivity;
import eggs.painted.fluppychat.Interface.OpenChat;
import eggs.painted.fluppychat.Model.Message;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 22.08.15.
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private final int USER_IMAGE_SIZE = 96;
    private Context context;
    private static List<ParseObject> roomList, peopleInRoomList;
    private OpenChat callback;

    public RoomAdapter( RoomActivity activity, List<ParseObject> roomList, List<ParseObject> peopleInRoomList) {
        this.roomList = roomList;
        this.peopleInRoomList = peopleInRoomList;
        this.context = activity.getApplicationContext();
        this.callback = (OpenChat) activity;
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
        if ( peopleInRoom.getBoolean("confirm") ) {
            contactViewHolder.initUI( r );
        } else  {
            contactViewHolder.initUI( peopleInRoom );
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo( "room", roomList.get(i) );
        query.include("people");
        query.whereEqualTo("confirm", true);
        Log.d("ROOM_NUM2", String.valueOf(i));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if ( null == e ) {
                    contactViewHolder.containerLayout.removeAllViews();
                    Log.d("ROOM", "receive people list");
                    for ( ParseObject o : list ) {
                        CircleImageView img = new CircleImageView(context);
                        img.setLayoutParams( new ViewGroup.LayoutParams( USER_IMAGE_SIZE, USER_IMAGE_SIZE ) );
                        UserImage.showImage( o.getParseUser("people"), img );
                        contactViewHolder.containerLayout.addView(img);
                    }
                }
            }
        });
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

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout containerLayout;
        protected Button confirmButton, declineButton;
        protected OpenChat callback;
        protected View v;
        protected ParseObject room, peopleInRoom;

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
                    try {
                        peopleInRoom.save();
                        callback.acceptRoom();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
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

    public static class RoomViewHolderConfirmed extends RoomViewHolder {
        public RoomViewHolderConfirmed( final OpenChat callback, View v ) {
            super(callback, v);
        }

        public void initUI( final ParseObject room ) {
            Log.d( "ROOM_ID", room.getObjectId() );
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d( "ROOM_ID", room.getObjectId() );
                    callback.openChat(room, peopleInRoom);
                }
            });
        }
    }
}

