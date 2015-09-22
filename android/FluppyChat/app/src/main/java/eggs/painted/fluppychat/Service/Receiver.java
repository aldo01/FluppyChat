package eggs.painted.fluppychat.Service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import eggs.painted.fluppychat.Activity.ChatActivity;
import eggs.painted.fluppychat.Activity.RoomActivity;
import eggs.painted.fluppychat.R;

public class Receiver extends ParsePushBroadcastReceiver {
    private final String TAG = "MY_RECEIVER";

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        Log.d( TAG, "onPushOpen" );

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // allow notifications
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.notificationKey), false);
        editor.commit();
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject pushData = null;
        Log.d(TAG, "receive message");

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        try {
            // decode message and put them in intent
            pushData = new JSONObject(intent.getStringExtra( KEY_PUSH_DATA ));
            Log.d(TAG, String.format("DATA: %s", pushData.toString()));
            final String chanel = intent.getStringExtra(KEY_PUSH_CHANNEL);

            Log.d(TAG, String.format("Channel: %s %s %s", chanel, String.valueOf(chanel.contains("ROOM")),
                    String.valueOf( isAppForground(context)) ));
            if ( chanel.contains("ROOM") && isAppForground(context) ) {
                Log.d(TAG, "Update room list1");
                if ( null != RoomActivity.self ) {
                    Log.d(TAG, "Update room list2");
                    RoomActivity.self.loadConversationsList();
                }
            }

            if ( isAppForground(context) ) {
                Log.d( "PUSH_DATA", pushData.toString() );
                final String msg = pushData.getString("Alert");
                final String authId = pushData.getString("Author");
                final String authName = pushData.getString("AuthorName");
                final String androidId = pushData.getString("AndroidId");

                Log.d(TAG, String.format("MESSAGE: %s", msg));
                Log.d(TAG, String.format("CHANEL: %s", chanel));

                ChatActivity.receiveMessage(msg, chanel, authId, authName, androidId);
            } else {
                // check value
                boolean showNotification = sharedPref.getBoolean(context.getString(R.string.notificationKey) + chanel, true);

                // save true value
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(context.getString(R.string.notificationKey) + chanel, false);

                // get prev count message and increment them
                int countMessage = sharedPref.getInt( context.getString(R.string.notificationCountKey) + chanel, 0);
                countMessage++;
                editor.putInt( context.getString(R.string.notificationCountKey) + chanel, countMessage );
                Log.d(TAG, String.format("message count %d for room %s", countMessage, context.getString(R.string.notificationCountKey) + chanel));
                editor.apply();


                Log.d(TAG, String.format("DATA2: count message %d, show notification %s", countMessage, String.valueOf(showNotification)));

                if ( showNotification ) {
                    // generate message text for notification
                    final String author = pushData.getString("AuthorName");
                    pushData.put("alert", String.format("You receive message from %s", author));
                    intent.putExtra(KEY_PUSH_DATA, pushData.toString());

                    super.onPushReceive(context, intent);
                }
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

        // allow notifications
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // save true value
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.notificationKey), false);
        editor.commit();
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return R.drawable.logo;
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
    }
}
