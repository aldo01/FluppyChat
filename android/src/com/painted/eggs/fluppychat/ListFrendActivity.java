package com.painted.eggs.fluppychat;

import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListFrendActivity extends Activity {
	private ListView frendsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_frends_activity);
		
		initUi();
		loadFrendList();
	}
	
	private void initUi() {
		frendsListView = (ListView) findViewById(R.id.frendsListViewListFrendsActivity);
		
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
	
	private void loadFrendList() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("relationship");
		query.whereEqualTo("owner", ParseUser.getCurrentUser());
		
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> list, ParseException e) {
		        if (e == null) {
		        	ParseUser userList[] = new ParseUser[ list.size() ];
		        	int i = 0;
		            for ( ParseObject obj : list ) {
		            	userList[i] = (ParseUser) obj.get("friend");
		            			            	
		            	i++;
		            	Log.d("Object:", obj.toString() );
		            }
		            
		            updateFrendsList(userList);
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
			}
		});
		
	}
	
	private void updateFrendsList( ParseUser [] userList ) {
		String name[] = new String[ userList.length ];
		for ( int i = 0; i < userList.length; i++ ) {
			//name[i] = userList[i].getUsername();
			name[i] = "123";
			
		}
		
		// create adapter
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	        android.R.layout.simple_list_item_1, name);

	    // присваиваем адаптер списку
	    frendsListView.setAdapter(adapter);
	}
}
