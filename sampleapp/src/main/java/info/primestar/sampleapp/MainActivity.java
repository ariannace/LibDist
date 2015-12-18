package info.primestar.sampleapp;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

import info.primestar.sampleapp.remotable.ApplyGreyscale;
import info.primestar.sampleapp.remotable.ReverseString;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    static final int SEND_TASK = 1;
    static final int SEND_DEX = 2;

    boolean mIsBinded;
    Messenger mMessenger;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
            mIsBinded = false;
            mServiceConnection = null;
            Log.v(TAG, "onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            // TODO Auto-generated method stub
            mIsBinded = true;
            mMessenger = new Messenger(arg1);
            Log.v(TAG, "onServiceConnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent mIntent = new Intent();
        mIntent.setAction("info.primestar.RemoteService");
        bindService(mIntent, mServiceConnection, BIND_AUTO_CREATE);
        Button mButton = (Button) findViewById(R.id.btn_send_dex);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Message msg = Message.obtain(null, SEND_DEX, 0, 0);
                Bundle data = new Bundle();
                try {
                    byte[] bytes = Commons.toByteArray(getAssets().open("secondary.dex"));
                    data.putByteArray("data", bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        Button task1Button = (Button) findViewById(R.id.btn_send_task_1);
        task1Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Message msg = Message.obtain(null, SEND_TASK, 0, 0);
                msg.replyTo = new Messenger(new ResponseHandler());
                Bundle data = new Bundle();
                data.putString("class", ReverseString.class.getName());
                String dataInput = "ArianNace";
                data.putByteArray("data", dataInput.getBytes());
                msg.setData(data);
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        Button task2Button = (Button) findViewById(R.id.btn_send_task_2);
        task2Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Message msg = Message.obtain(null, SEND_TASK, 0, 0);
                msg.replyTo = new Messenger(new ResponseHandler());
                Bundle data = new Bundle();
                data.putString("class", ApplyGreyscale.class.getName());

                data.putByteArray("data", Commons.toByteArray(getResources().openRawResource(R.raw.tulips)));
                msg.setData(data);
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    // This class handles the Service response
    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            byte[] data = msg.getData().getByteArray("risp");
            if(data!=null){
                ((ImageView)findViewById(R.id.image)).setImageBitmap(
                        BitmapFactory.decodeByteArray(data,0, data.length)
                );
            }else{
                Log.v("TAG","RESULT_CANCELED");
            }
        }
    }
}
