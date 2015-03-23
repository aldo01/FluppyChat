package com.painted.eggs.fluppychat;

import com.painted.eggs.fluppychat.sinch.MessageService;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	private EditText loginEditText, passwordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize( getApplicationContext(), getString(R.string.parseApplicationId), getString(R.string.parseClientKey));
		setContentView(R.layout.sign_in_activity);		
		
		initUI();
	}
	
	private void initUI() {
		final Button signInButton = (Button) findViewById(R.id.signInButtonSignInActivity);
		signInButton.setOnClickListener(this);
		
		final Button signUpButton = (Button) findViewById(R.id.registerButtonSignInActivity);
		signUpButton.setOnClickListener(this);
		
		loginEditText = (EditText) findViewById(R.id.usernameEditTextSignInActivity);
		passwordEditText = (EditText) findViewById(R.id.passwordEditTextSignInActivity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		
		switch( v.getId() ) {
		case R.id.signInButtonSignInActivity:
			String username = loginEditText.getText().toString();
			String password = passwordEditText.getText().toString();
			
			ParseUser.logInInBackground(username, password, new LogInCallback() {
				
				@Override
				public void done(ParseUser user, ParseException arg1) {
	
			        if ( null != user ) {
			        	Log.d("USER", "user is defined");
			        	// open list view activity
				    	final Intent i = new Intent(MainActivity.this, ListFrendActivity.class);
			        	final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
			        	
			        	startService(serviceIntent);
				    	startActivity(i);
			        } else {
			        	Log.d("USER", "user is not defined");
			        	Toast.makeText(getApplicationContext(), "bad login", Toast.LENGTH_SHORT).show();
			        }
				}


			});
			break;
			
		case R.id.registerButtonSignInActivity:
			break;
		}
		
	}
}
