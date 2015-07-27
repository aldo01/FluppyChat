package com.sinch.messagingtutorial.app.Adapters;

import java.util.List;

import android.util.Log;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParsePush;
import com.sinch.messagingtutorial.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class RoomAdapter extends ArrayAdapter<ParseObject> {
	private Context myContext;
    private List<ParseObject> objLsit;

    public RoomAdapter( Context context, List<ParseObject> _objList ) {
        super( context, R.layout.user_list_item, _objList );

        myContext = context;
        objLsit = _objList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ParseObject pRoom = objLsit.get(position);
        final ParseObject room = pRoom.getParseObject("room");
        View rowView;
        if ( pRoom.getBoolean("confirm") ) {
            rowView = inflater.inflate(R.layout.user_list_item, parent, false);
            // set information in cell
            final TextView nameTextView = (TextView) rowView.findViewById( R.id.userListItem );
//            nameTextView.setText( room.getString("Name") );
        } else {
            rowView = inflater.inflate(R.layout.user_list_confirm_item, parent, false);

            // show room name
           // final TextView nameTextView = (TextView) rowView.findViewById( R.id.userListItemConfirm );
           // nameTextView.setText(room.getString("Name"));

            final Button acceptButton = (Button) rowView.findViewById(R.id.acceptButton);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pRoom.put("confirm", true);
                    pRoom.saveInBackground();
                    if ( null != room ) {
                        ParsePush.subscribeInBackground( room.getObjectId() );
                    }
                }
            });

            final Button declineButton = (Button) rowView.findViewById(R.id.declineButton);
            declineButton.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    pRoom.deleteInBackground();
                }
            });
        }

        return rowView;
    }
}
