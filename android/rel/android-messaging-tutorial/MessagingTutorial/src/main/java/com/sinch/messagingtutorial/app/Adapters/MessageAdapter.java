package com.sinch.messagingtutorial.app.Adapters;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scottyab.aescrypt.AESCrypt;
import com.sinch.android.rtc.messaging.WritableMessage;
import com.sinch.messagingtutorial.app.R;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Pair<WritableMessage, Integer>> messages;
    private List<String> userName;
    private LayoutInflater layoutInflater;

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<Pair<WritableMessage, Integer>>();
        userName = new ArrayList<String>();
    }

    public void addMessage(WritableMessage message, int direction, String name) {
        messages.add(new Pair(message, direction));
        userName.add( name );
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        int direction = getItemViewType(i);

        //show message on left or right, depending on if
        //it's incoming or outgoing
        if (convertView == null) {
            int res = 0;
            if (direction == DIRECTION_INCOMING) {
                res = R.layout.message_right;
            } else if (direction == DIRECTION_OUTGOING) {
                res = R.layout.message_left;
            }
            convertView = layoutInflater.inflate(res, viewGroup, false);
        }

        WritableMessage message = messages.get(i).first;

        // decode message
        String password = "password";
        String messageAfterDecrypt = "";
        try {
            messageAfterDecrypt = AESCrypt.decrypt(password, message.getTextBody());
        } catch (GeneralSecurityException e){
            e.printStackTrace();
            Log.e("DECODE_MESSAGE", "Error when decode message");
        } catch ( Exception e ){
            e.printStackTrace();
            Log.e( "DECODE_MESSAGE", "Error when decode message" );
        }

        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        txtMessage.setText( messageAfterDecrypt );

        TextView txtSender = (TextView) convertView.findViewById(R.id.txtSender);
        txtSender.setText( userName.get(i) );

        return convertView;
    }
}
