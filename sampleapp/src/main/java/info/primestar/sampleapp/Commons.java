package info.primestar.sampleapp;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Arian Nace on 08/12/2015.
 */
public class Commons {
    public static byte[] toByteArray(InputStream is){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try{
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }
}
