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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import eggs.painted.fluppychat.Model.Message;
import eggs.painted.fluppychat.R;
import eggs.painted.fluppychat.Util.Decoder;
import eggs.painted.fluppychat.Util.UserImage;

/**
 * Created by dmytro on 23.08.15.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    static private int mGMTOffset = -1; // time offset from GMT+0

    private Context context;
    private List<Message> messageList;
    private ParseUser myUser;
    private boolean animate = false;
    // Animation
    Animation animFromMiddle, animToMiddle;

    public MessageAdapter( Context context, List<Message> messageList) {
        this.messageList = messageList;
        this.context = context;

        if ( -1 == mGMTOffset ) {
            // init time offset from grinvich
            Calendar mCalendar = new GregorianCalendar();
            TimeZone mTimeZone = mCalendar.getTimeZone();
            mGMTOffset = mTimeZone.getRawOffset();
        }

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

        contactViewHolder.messageTV.setText( Decoder.decodeMessage(m.text) );
        if ( !m.saved ) {
            // enable text view that message is not sent
            contactViewHolder.notSentMessageTV.setVisibility(View.VISIBLE);
        }

        if ( null != u ) {
            // incomming message
            UserImage.showImage(u, contactViewHolder.userIV);
            contactViewHolder.userNameTV.setText(u.getUsername());
            /*
            long timeMil = m.date.getTime();
            timeMil = (timeMil - mGMTOffset) * 1000L;
            */
            contactViewHolder.dateTV.setText( DateFormat.getDateTimeInstance().format(m.date) );
        } else {
            // my message
            UserImage.showImage(m.userId, contactViewHolder.userIV);
            contactViewHolder.userNameTV.setText( m.userName );
            contactViewHolder.dateTV.setText( DateFormat.getDateTimeInstance().format(new Date()) );
        }

        if ( animate && i == messageList.size() - 1 ) {
            contactViewHolder.itemView.startAnimation(animToMiddle);
            animate = false;
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
        protected TextView notSentMessageTV;

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
            notSentMessageTV = (TextView) v.findViewById(R.id.notSavedMessageTVMyMessageCell);
        }
    }

    public static class FriendMessageViewHolder extends MessageViewHolder {

        public FriendMessageViewHolder(View v) {
            super(v);
            userIV = (CircleImageView) v.findViewById(R.id.userImageViewFriendMessageCell);
            messageTV = (TextView) v.findViewById(R.id.messageTVFriendMessageCell);
            dateTV = (TextView) v.findViewById(R.id.dateTVFriendMessageCell);
            userNameTV = (TextView) v.findViewById(R.id.userNameTVFriendMessageCell);
            notSentMessageTV = (TextView) v.findViewById(R.id.notSavedMessageTVFriendMessageCell);
        }
    }

    public void showAnimation() {
        animate = true;
    }
}
