package com.sinch.messagingtutorial.app.Adapters;

import java.util.Arrays;
import java.util.List;

import android.util.Log;
import android.view.View;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.messaging.WritableMessage;
import com.sinch.messagingtutorial.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        View rowView = inflater.inflate( R.layout.user_list_item, parent, false);

        // set information in cell
        final TextView nameTextView = (TextView) rowView.findViewById( R.id.userListItem );
        nameTextView.setText( "Friends" );

        ParseObject obj = objLsit.get(position);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
        query.whereEqualTo("room", obj);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    String title = "";

                    for ( ParseObject o : list) {

                        String name = "";
                        try {
                            ParseUser u = o.getParseUser("people");
                            name = u.fetchIfNeeded().getString("username");
                            title += name + ", ";
                        } catch (com.parse.ParseException err) {
                            Log.v("PARSE_ERROR", err.toString());
                            e.printStackTrace();
                        }
                    }

                    nameTextView.setText( title );
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });




        return rowView;
    }
}
