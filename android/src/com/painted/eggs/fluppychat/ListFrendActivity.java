package com.painted.eggs.fluppychat;

import java.util.ArrayList;
import java.util.List;

import com.painted.eggs.fluppychat.adapters.ParseUserAdapter;
import com.painted.eggs.fluppychat.adapters.RoomAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ListFrendActivity extends Activity implements View.OnClickListener {
	private ListView frendsListView;
	List<ParseObject> roomList = new ArrayList<ParseObject>();
	RoomAdapter cellAdapter;
	
	private EditText loginEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_frends_activity);
		
		initUi();
		initListView();
		loadFrendList();
	}
	
	private void initUi() {
		
		loginEditText = (EditText) findViewById(R.id.nameEditTextMyRoomsActivity);
		
		final Button serchButton = (Button) findViewById( R.id.serchButtonMyRoomsActivity );
		serchButton.setOnClickListener( this );
		
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Loading");
		progressDialog.setMessage("Please wait...");
		progressDialog.show();
		//broadcast receiver to listen for the broadcast
		//from MessageService
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Boolean success = intent.getBooleanExtra("success", false);
		        progressDialog.dismiss();
		        //show a toast message if the Sinch
		        //service failed to start
		        if (!success) {
		            Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
		        }				
			}
		};
		
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.sinch.messagingtutorial.app.ListUsersActivity"));
	}
	
	/**
	 * Init list view.
	 * Show all user rooms
	 */			
	private void initListView() {
		ListView myList = (ListView) findViewById(R.id.frendsListViewListFrendsActivity);
		
		cellAdapter = new RoomAdapter( getApplicationContext(), roomList );
		myList.setAdapter( cellAdapter );
		myList.setOnItemClickListener( clickListener );
	}
	
	/**
	 * Create room with user
	 */
	AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
        }
    };
	
	private void loadFrendList() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("PeopleInRoom");
		query.whereEqualTo("people", ParseUser.getCurrentUser() );
		
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> list, ParseException e) {
		        if (e == null) {
		        	
		            for ( ParseObject obj : list ) {
		            	// roomList.add(obj);
		            	cellAdapter.add(obj);	            	
		            }
		            
		            
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		            e.printStackTrace();
		        }
			}
		});
		
	}
	
	private void updateFrendsList( ParseUser [] userList ) {
		String name[] = new String[ userList.length ];
		for ( int i = 0; i < userList.length; i++ ) {
			name[i] = userList[i].getObjectId();
			//name[i] = "123";
		}
		
		// create adapter
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	        android.R.layout.simple_list_item_1, name);

	    // присваиваем адаптер списку
	    frendsListView.setAdapter(adapter);
	}
	
/*	public void openConversation( int pos ) {
	    Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra("RECIPIENT_ID", userList[pos].getObjectId() );
        startActivity(intent);
	}*/

	@Override
	public void onClick(View v) {
		
		switch( v.getId() ) {
		case R.id.serchButtonMyRoomsActivity:
			
			final Intent searchPeopleIntent = new Intent( ListFrendActivity.this, SearchPeopleActivity.class );
			searchPeopleIntent.putExtra("login", loginEditText.getText().toString() );
			startActivity( searchPeopleIntent );
			
			break;
		}
		
	}
}
