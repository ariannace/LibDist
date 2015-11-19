package info.primestar.sampleapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.Enumeration;

import dalvik.system.DexFile;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String sourceDir = getApplicationInfo().sourceDir;
        try{
            DexFile dexFile = new DexFile(sourceDir);
            Enumeration<String> entries = dexFile.entries();
            String entry;
            while (entries.hasMoreElements()){
                entry = entries.nextElement();
                if(entry.contains("info.primestar"))
                    Log.v(TAG,entry);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

    }


}
