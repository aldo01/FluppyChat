package eggs.painted.fluppychat.Util;

import android.util.Log;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

/**
 * Created by dmytro on 27.07.15.
 */
final public class Decoder {
    static private String PASSWORD = "password";

    /**
     * Code text
     *
     * @param text - message for coding
     * @return - coded message
     */
    static public String codeMessage( final String text ) {
        String encryptedMsg = null;

        try {
            encryptedMsg = AESCrypt.encrypt( PASSWORD, text );
        }catch (GeneralSecurityException e){
            e.printStackTrace();
            Log.e("ECNCRYPT_ERROR", "error ocqurence when encrypt message");
            //handle error
        }

        return encryptedMsg;
    }

    /**
     * Decode text
     *
     * @param text - message for decoding
     * @return - decoded message
     */
    static public String decodeMessage( final String text ) {
        String msg = null;

        try {
            msg = AESCrypt.decrypt(PASSWORD, text);
        } catch (GeneralSecurityException e){
            e.printStackTrace();
            Log.e("DECODE_MESSAGE", "Error when decode message");
        } catch ( Exception e ){
            e.printStackTrace();
            Log.e( "DECODE_MESSAGE", "Error when decode message" );
        }

        return msg;
    }
}
