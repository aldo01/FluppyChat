package com.sinch.messagingtutorial.app.Feature;

import android.app.Activity;
import android.util.Log;

/**
 * Created by dmytro-b on 24.05.15.
 */
public class MyProgressDialog {
    static private android.app.ProgressDialog progressDialog;
    static private boolean show = false;
    static private int showCount = 0;

    static public void start( Activity a ) {
        if ( !show ) {
            progressDialog = new android.app.ProgressDialog(a);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            show = true;
            showCount = 1;
        } else {
            showCount += 1;
        }

        Log.d( "MyProgressDialog", String.valueOf(showCount) );
    }

    static public void stop() {
        if ( show ) {
            showCount--;
            if ( 0 == showCount ) {
                progressDialog.dismiss();
                show = false;
            }
        }

        Log.d( "MyProgressDialog", String.valueOf(showCount) );
    }

    static public boolean isShowed() {
        return show;
    }

}
