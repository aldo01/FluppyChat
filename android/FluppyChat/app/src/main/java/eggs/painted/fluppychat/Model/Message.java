package eggs.painted.fluppychat.Model;

import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by dmytro on 23.08.15.
 */
public class Message {
    public String text;
    public Date date;
    public ParseUser user;
    public String userName;
    public String userId;
}
