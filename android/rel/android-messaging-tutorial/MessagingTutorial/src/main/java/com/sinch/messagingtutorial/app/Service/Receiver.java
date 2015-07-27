package com.sinch.messagingtutorial.app.Service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.sinch.messagingtutorial.app.MessagingActivity;
import com.sinch.messagingtutorial.app.Util.Decoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Receiver extends ParsePushBroadcastReceiver {
    private final String TAG = "MY_RECEIVER";


    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        Log.d( TAG, "onPushOpen" );
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject pushData = null;
        Log.d( TAG, "receive message" );

        try {
            // decode message and put them in intent
            pushData = new JSONObject(intent.getStringExtra( KEY_PUSH_DATA ));
            Log.d(TAG, String.format("DATA: %s", pushData.toString()));

            if ( isAppForground(context) ) {
                final String chanel = intent.getStringExtra(KEY_PUSH_CHANNEL);
                final String msg = pushData.getString("Alert");
                final String authId = pushData.getString("Author");
                final String authName = pushData.getString("AuthorName");
                Log.d( TAG, String.format( "MESSAGE: %s", msg ) );
                Log.d( TAG, String.format("CHANEL: %s", chanel ) );

                MessagingActivity.receiveMessage( msg, chanel, authId, authName );
            } else {
                final String msg = Decoder.decodeMessage( pushData.getString("Alert") );
                pushData.put( "alert", msg );
                intent.putExtra( KEY_PUSH_DATA, pushData.toString() );

                super.onPushReceive(context, intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ceck if app runing in foreground or background
     *
     * @return - true foreground, false background
     */
    public boolean isAppForground(Context mContext) {

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
        Log.d(TAG, "onPushDismiss");
    }
}
