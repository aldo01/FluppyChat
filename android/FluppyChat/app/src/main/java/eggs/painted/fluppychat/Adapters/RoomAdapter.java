package eggs.painted.fluppychat.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Activity.RoomActivity;
import eggs.painted.fluppychat.Interface.OpenChat;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 22.08.15.
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private final int USER_IMAGE_SIZE = 96;
    private Context context;
    private List<ParseObject> roomList;
    private OpenChat callback;

    public RoomAdapter( RoomActivity activity, List<ParseObject> roomList) {
        this.roomList = roomList;
        this.context = activity.getApplicationContext();
        this.callback = (OpenChat) activity;
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    @Override
    public void onBindViewHolder(final RoomViewHolder contactViewHolder, int i) {
        contactViewHolder.initUI( roomList.get(i) );

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo( "room", roomList.get(i) );
        query.include("people");
        Log.d("ROOM_NUM2", String.valueOf(i));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if ( null == e ) {
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
    public RoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.room_cell_layout, viewGroup, false);

        RoomViewHolder holder = new RoomViewHolder(callback, itemView );
        Log.d( "ROOM_NUM", String.valueOf(i) );
        return holder;
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout containerLayout;
        private OpenChat callback;
        private View v;

        public RoomViewHolder( final OpenChat callback, View v ) {
            super(v);
            this.v = v;
            this.callback = callback;
            containerLayout =  (LinearLayout) v.findViewById(R.id.imageContainerLayout);
        }

        public void initUI( final ParseObject room ) {
            Log.d( "ROOM_ID", room.getObjectId() );
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d( "ROOM_ID", room.getObjectId() );
                    callback.openChat(room);
                }
            });
        }
    }
}

