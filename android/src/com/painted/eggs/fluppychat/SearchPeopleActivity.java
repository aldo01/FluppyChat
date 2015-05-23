package com.painted.eggs.fluppychat;

import java.util.ArrayList;
import java.util.List;

import com.painted.eggs.fluppychat.adapters.ParseUserAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class SearchPeopleActivity extends Activity {
	private ParseUserAdapter cellAdapter;
	private List<ParseUser> userList = new ArrayList<ParseUser>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView( R.layout.search_people_layout );
		
		String login = "lera";
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			login = extras.getString("login");
		} 
		
		initListView();
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", login);
		query.findInBackground(new FindCallback<ParseUser>() {
		  public void done(List<ParseUser> objects, ParseException e) {
		    if (e == null) {
		        
		    	for ( ParseUser u : objects ) {
		    		cellAdapter.add(u);
		    	}
		    	
		    } else {
		        // Something went wrong.
		    	e.printStackTrace();
		    }
		  }
		});
	}
	
	private void initListView() {
		ListView myList = (ListView) findViewById(R.id.peopleListViewSearchPeopleActivity);
		
		cellAdapter = new ParseUserAdapter( getApplicationContext(), userList);
		myList.setAdapter( cellAdapter );
	}
	
	
	
}
