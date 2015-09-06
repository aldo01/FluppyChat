package eggs.painted.fluppychat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "***", "***");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
