package eggs.painted.fluppychat.Interface;

import com.parse.ParseObject;

/**
 * Created by dmytro on 23.08.15.
 */
public interface OpenChat {
    void openChat(ParseObject room, ParseObject peopleInRoom );
    void acceptRoom();
    void declineRoom();
}
