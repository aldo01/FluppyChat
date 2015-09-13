package eggs.painted.fluppychat.Util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by dmytrobohachevskyy on 9/13/15.
 */
final public class Toaster {

    public static void showText( Context c, String text ) {
        Toast.makeText( c, text, Toast.LENGTH_LONG ).show();
    }

}
