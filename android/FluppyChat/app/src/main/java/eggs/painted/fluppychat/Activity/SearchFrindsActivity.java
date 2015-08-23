package eggs.painted.fluppychat.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import eggs.painted.fluppychat.Adapters.FriendCellAdapter;
import eggs.painted.fluppychat.R;

/**
 * Created by dmytro on 23.08.15.
 */
public class SearchFrindsActivity extends Activity {
    FriendCellAdapter cellAdapter;

    // ui objects
    RecyclerView recList;
    EditText loginET;
    ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_friend_activity);

        initUI();
    }

    private void initUI() {
        recList = (RecyclerView) findViewById(R.id.friendsList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        loginET = (EditText) findViewById(R.id.loginSerchET);
        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        final Button findButton = (Button) findViewById(R.id.findFriendButton);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPeople();
            }
        });
    }

    private void searchPeople() {
        wheel.setVisibility(View.VISIBLE);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", loginET.getText().toString() );
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    cellAdapter = new FriendCellAdapter( getApplicationContext(), objects );
                    recList.setAdapter( cellAdapter );
                } else {
                    // Something went wrong.
                    e.printStackTrace();
                }

                wheel.setVisibility(View.GONE);
            }
        });
    }
}
