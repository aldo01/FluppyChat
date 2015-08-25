package eggs.painted.fluppychat.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.Dictionary;
import java.util.Hashtable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dmytro on 23.08.15.
 * Use for saving user images
 */
final public class UserImage {
    static private Hashtable<String, Bitmap> userImages = new Hashtable<String, Bitmap>();

    static private void downloadImage( final ParseUser user, final CircleImageView img ) {
        ParseFile file = user.getParseFile("profilepic");
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                if ( null == e ) {
                    Log.d("IMAGE", "loaded");
                    userImages.put(user.getObjectId(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length) );
                    img.setImageBitmap(userImages.get(user.getObjectId()));
                } else {
                    Log.e( "PARSE", "Load image error" );
                    e.printStackTrace();
                }
            }
        });
    }

    static public void showImage( final ParseUser user, final CircleImageView img ) {
        Log.d( "SHOW_IMAGE", user.getObjectId() );
        if ( !userImages.containsKey(user.getObjectId()) ) {
            downloadImage( user, img );
        } else {
            img.setImageBitmap( userImages.get(user.getObjectId()) );
        }
    }

    static public void showImage( final String userId, final CircleImageView img ) {
        if ( null != userImages ) {
            if (userImages.containsKey(userId)) {
                img.setImageBitmap(userImages.get(userId));
            }
        }
    }
}
