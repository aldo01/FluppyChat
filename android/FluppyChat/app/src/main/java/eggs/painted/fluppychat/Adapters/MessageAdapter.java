package eggs.painted.fluppychat.Adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Model.Message;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.Decoder;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 23.08.15.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<Message> messageList;
    private ParseUser myUser;
    private boolean animate = false;
    // Animation
    Animation animFromMiddle, animToMiddle;

    public MessageAdapter( Context context, List<Message> messageList) {
        this.messageList = messageList;
        this.context = context;

        myUser = ParseUser.getCurrentUser();
        animFromMiddle  = AnimationUtils.loadAnimation(context, R.anim.from_middle);
        animToMiddle  = AnimationUtils.loadAnimation( context, R.anim.to_middle );
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder contactViewHolder, int i) {
        Message m = messageList.get(i);
        ParseUser u = m.user;
        Log.d("BIND_CELL", String.valueOf(i));

        contactViewHolder.messageTV.setText( Decoder.decodeMessage(m.text) );

        if ( null != u ) {
            Log.d( "BIND_CELL", "user is not null" );
            UserImage.showImage( u, contactViewHolder.userIV );
            contactViewHolder.userNameTV.setText( u.getUsername() );
            // contactViewHolder.dateTV.setText( u.getUpdatedAt().toString() );
        } else {
//            contactViewHolder.userNameTV.setText(u.getUsername());
            Log.d( "BIND_CELL", "user is null");
            UserImage.showImage(m.userId, contactViewHolder.userIV);
            contactViewHolder.userNameTV.setText( m.userName );
            // contactViewHolder.dateTV.setText(m.date.toString());
        }

        if ( animate && i == messageList.size() - 1 ) {
            contactViewHolder.itemView.startAnimation(animToMiddle);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message m = messageList.get(position);
        String id;
        if ( null == m.user ) {
            id = m.userId;
        } else {
            id = m.user.getObjectId();
        }

        Log.d( "CREATE_CELL", id);
        if ( id.equals(myUser.getObjectId()) ) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if ( 0 == i ) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.my_message, viewGroup, false);

            return new MyMessageViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.friend_message, viewGroup, false);

            return new FriendMessageViewHolder(itemView);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        protected CircleImageView userIV;
        protected TextView messageTV;
        protected TextView dateTV;
        protected TextView userNameTV;

        public MessageViewHolder(View v) {
            super(v);
        }
    }

    public static class MyMessageViewHolder extends MessageViewHolder {

        public MyMessageViewHolder(View v) {
            super(v);
            userIV = (CircleImageView) v.findViewById(R.id.userImageViewMyMessageCell);
            messageTV = (TextView) v.findViewById(R.id.messageTVMyMessageCell);
            dateTV = (TextView) v.findViewById(R.id.dateTVMyMessageCell);
            userNameTV = (TextView) v.findViewById(R.id.userNameTVMyMessageCell);
        }
    }

    public static class FriendMessageViewHolder extends MessageViewHolder {

        public FriendMessageViewHolder(View v) {
            super(v);
            userIV = (CircleImageView) v.findViewById(R.id.userImageViewFriendMessageCell);
            messageTV = (TextView) v.findViewById(R.id.dateTVFriendMessageCell);
            dateTV = (TextView) v.findViewById(R.id.messageTVFriendMessageCell);
            userNameTV = (TextView) v.findViewById(R.id.userNameTVFriendMessageCell);
        }
    }

    public void showAnimation() {
        animate = true;
    }
}
