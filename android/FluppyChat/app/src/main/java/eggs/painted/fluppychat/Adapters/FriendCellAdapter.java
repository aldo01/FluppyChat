package eggs.painted.fluppychat.Adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 23.08.15.
 */
public class FriendCellAdapter extends RecyclerView.Adapter<FriendCellAdapter.FriendViewHolder> {
    private Context context;
    private List<ParseUser> userList;

    public FriendCellAdapter( Context context, List<ParseUser> userList) {
        this.userList = userList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder contactViewHolder, int i) {
        final ParseUser u = userList.get(i);
        contactViewHolder.userTV.setText(u.getUsername());

        ParseFile file = u.getParseFile("profilepic");
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                if (null == e) {
                    contactViewHolder.userIV.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                } else {
                    Log.e("PARSE", "Load image error");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.friend_card, viewGroup, false);

        return new FriendViewHolder(itemView);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        protected CircleImageView userIV;
        protected TextView userTV;

        public FriendViewHolder(View v) {
            super(v);
            userIV = (CircleImageView) v.findViewById(R.id.userImageView);
            userTV = (TextView) v.findViewById(R.id.userNameTV);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d( "CLICK", "CLICK" );
                }
            });
        }
    }
}
