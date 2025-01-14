package eggs.painted.fluppychat.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;

import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.Toaster;

public class LoginActivity extends Activity {
    private EditText usernameField;
    private EditText passwordField;
    private String username;
    private String password;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(getApplicationContext(), RoomActivity.class);

        // allow notifications
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // save true value
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean( getString(R.string.notificationKey), false);
        editor.commit();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            startActivity(intent);
        }

        setContentView(R.layout.activity_login);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button signUpButton = (Button) findViewById(R.id.signupButton);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameField.getText().toString();
                password = passwordField.getText().toString();

                // check if fields is not empty
                if ( username.isEmpty() || password.isEmpty() ) {
                    Toaster.showText( getApplicationContext(), "Some fields is empty" );
                    return;
                }

                // check withespace in username
                if ( containsWhiteSpace(username) ) {
                    Toast.makeText(getApplicationContext(),
                            "Incorrect User Name."
                            , Toast.LENGTH_LONG).show();
                    return;
                }

                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    public void done(ParseUser user, com.parse.ParseException e) {
                        if (user != null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Wrong username/password combo",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameField.getText().toString();
                password = passwordField.getText().toString();

                // check if fields is not empty
                if ( username.isEmpty() || password.isEmpty() ) {
                    Toaster.showText( getApplicationContext(), "Some fields is empty" );
                    return;
                }

                // check withespace in username
                if ( containsWhiteSpace(username) ) {
                    Toast.makeText(getApplicationContext(),
                            "Incorrect User Name."
                            , Toast.LENGTH_LONG).show();
                    return;
                }

                // upload default user photo
                byte [] imageData = getBytesFromBitmap(BitmapFactory.decodeResource( getResources(), R.mipmap.user_photo ));
                ParseFile imageFile = new ParseFile( imageData );
                try {
                    imageFile.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                ParseUser user = new ParseUser();
                user.setUsername(username);
                user.setPassword(password);
                user.put( "profilepic", imageFile );
                user.signUpInBackground(new SignUpCallback() {
                    public void done(com.parse.ParseException e) {
                        if (e == null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "There was an error signing up."
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * Check if string contain space character
     *
     * @param testCode - string for checking
     */
    private boolean containsWhiteSpace(final String testCode){
        if(testCode != null){
            // first is not digit
            if ( Character.isDigit(testCode.charAt(0)) ) {
                return true;
            }

            for(int i = 0; i < testCode.length(); i++){
                char c = testCode.charAt(i);
                // is alphabetical or digit
                Log.d( "LOGIN", String.format("%s %s", String.valueOf(Character.isAlphabetic(c)),
                        String.valueOf(Character.isDigit(c) )));
                if ( !Character.isAlphabetic(c) && !Character.isDigit(c) ){
                    Log.d( "LOGIN", "contain whitespace" );
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * convert from bitmap to byte array
     */
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onResume() {
        super.onResume();
        usernameField.setText("");
        passwordField.setText("");
    }
}
