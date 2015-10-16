package eggs.painted.fluppychat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "fYiaMJQcSGKjQB3AwhpGmpFoBvE8UiLJAAQMGKjh", "t5lfmPcRZjfRHnBGlPYS984ahstd1nHriMdirpA9");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
