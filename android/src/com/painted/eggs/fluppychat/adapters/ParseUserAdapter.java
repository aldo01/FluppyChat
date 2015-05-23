package com.painted.eggs.fluppychat.adapters;

import java.util.List;

import com.painted.eggs.fluppychat.R;
import com.parse.ParseUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ParseUserAdapter extends ArrayAdapter<ParseUser> {
	private Context myContext;
    private List<ParseUser> userLsit;

    public ParseUserAdapter( Context context, List<ParseUser> _userList ) {
        super( context, R.layout.user_cell, _userList );

        myContext = context;
        userLsit = _userList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate( R.layout.user_cell, parent, false);

        ParseUser obj = userLsit.get(position);
        
        // set information in cell
        final TextView nameTextView = (TextView) rowView.findViewById( R.id.userNameTextView );
        nameTextView.setText( obj.getUsername() );

        return rowView;
    }
}
