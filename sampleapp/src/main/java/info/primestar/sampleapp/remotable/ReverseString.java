package info.primestar.sampleapp.remotable;

import android.util.Log;

import info.primestar.framework.RemotableTask;

public class ReverseString implements RemotableTask {

    public byte[] performTask(byte[] chars) {
        Log.v("ReverseString",new String(chars));
        byte temp;
        for(int i=0;i<chars.length/2;i++){
            temp = chars[i];
            chars[i] = chars[chars.length-1-i];
            chars[chars.length-1-i] = temp;
        }
        Log.v("ReverseString",new String(chars));
        return chars;
    }

}