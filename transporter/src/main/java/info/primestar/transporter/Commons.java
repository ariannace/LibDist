package info.primestar.transporter;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Arian Nace on 10/12/2015.
 */
public class Commons {
    public static final String TAG = "Commons";
    public static final int SOCKET_TIMEOUT = 5000;
    public static final String DEX_RECEIVER_PORT = "8999";
    public static final String TASK_RECEIVER_PORT = "12345";
   // public static final int REMOTE_TASK_REQUEST_PORT = 9889;

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }
}
