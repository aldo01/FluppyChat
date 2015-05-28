package com.sinch.messagingtutorial.app.Adapters;

import java.util.Arrays;
import java.util.List;

import android.util.Log;
import android.view.View;

import com.parse.FindCallback;
import com.parse.ParseException;
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

        String name = "";
        try {
            name = objLsit.get(position).fetchIfNeeded().getString("Name");

        } catch (ParseException e) {
            e.printStackTrace();
        }
        nameTextView.setText( name );

        return rowView;
    }
}
