package eggs.painted.fluppychat.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Activity.ChatActivity;
import eggs.painted.fluppychat.Interface.AddPeopleToRoom;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 26.08.15.
 */
public class UserHereAdapter extends ArrayAdapter<ParseUser> {
    private Context myContext;
    private List<ParseUser> userLsit;
    private AddPeopleToRoom callback;

    public UserHereAdapter( ChatActivity activity, List<ParseUser> _userList ) {
        super( activity.getApplicationContext(), R.layout.friend_card, _userList );

        callback = (AddPeopleToRoom) activity;
        myContext = activity.getApplicationContext();
        userLsit = _userList;
    }

    @Override
    public int getCount() {
        return userLsit.size() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_card, parent, false);
        final CircleImageView imageView = (CircleImageView) rowView.findViewById(R.id.userImageView);
        final TextView nameTextView = (TextView) rowView.findViewById(R.id.userNameTV);
        nameTextView.setTypeface(Typeface.DEFAULT);
        if ( position < userLsit.size() ) {
            ParseUser obj = userLsit.get(position);

            nameTextView.setText(obj.getUsername());
            UserImage.showImage(obj, imageView);
            return rowView;
        } else {
            nameTextView.setText( "Add people" );
            imageView.setImageResource(R.mipmap.ic_add);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.addPeople();
                }
            });
            return rowView;
        }
    }
}

