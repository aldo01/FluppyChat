package com.painted.eggs.fluppychat;

import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ListFrendActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_frends_activity);
		
		updateFrendList();
	}
	
	private void updateFrendList() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("relationship");
		query.whereEqualTo("owner", ParseUser.getCurrentUser());
		
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> list, ParseException e) {
		        if (e == null) {
		            for ( ParseObject obj : list ) {
		            	Log.d("Object:", obj.toString() );
		            }
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
			
			}
		});
		
	}
}
