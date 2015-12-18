package info.primestar.transporter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import dalvik.system.DexClassLoader;
import info.primestar.framework.RemotableTask;
import info.primestar.framework.TaskDescription;
import info.primestar.transporter.threads.DexReceiverThread;
import info.primestar.transporter.threads.TaskReceiverThread;
import info.primestar.transporter.threads.TaskRequestThread;

public class RemoteService extends Service implements
        WifiP2pManager.ActionListener,
        WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener{
    public static final String TAG = "RemoteService";
    static final int SEND_TASK = 1, SEND_DEX = 2, REPLY = 3;
    private static final String SECONDARY_DEX_NAME = "secondary.dex";

    /* HERE STARTS */
    private final IntentFilter intentFilter = new IntentFilter();
    Messenger mMessenger = new Messenger(new MyHandler());
    DexClassLoader cl;
    WifiP2pManager.Channel mChannel;
    protected WifiP2pManager mManager;
    private BroadcastReceiver receiver = null;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.v(TAG, "onBind()");
        return mMessenger.getBinder();
    }

    @Override
    public void onSuccess() {
        Log.v(TAG, "onSuccess()");
    }

    @Override
    public void onFailure(int reason) {
        Log.v(TAG, "onFailure()");
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.v(TAG, "onPeersAvailable()");
    }

    WifiP2pInfo info;
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.v(TAG, "onConnectionInfoAvailable()");
        this.info = info;
        Log.d("isGroupOwner", String.valueOf(info.isGroupOwner));
        Log.d("groupOwnerAddress", String.valueOf(info.groupOwnerAddress.getHostAddress()));
        if (info.groupFormed && info.isGroupOwner) {
            //new DexReceiverAsyncTask(this).execute();
            DexReceiverThread dexReceiverThread = new DexReceiverThread(this);
            dexReceiverThread.start();
            TaskReceiverThread taskReceiverThread = new TaskReceiverThread(this);
            taskReceiverThread.start();
        }
    }

    byte[] resultPointer = new byte[1024];

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "handleMessage()");
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_TASK:
                    Toast.makeText(getApplicationContext(), "FRAMEWORK: TASK RECEIVED", Toast.LENGTH_LONG).show();
                    try {
                        Class libProviderClazz = null;
                        libProviderClazz = cl.loadClass(msg.getData().getString("class"));
                        RemotableTask task = (RemotableTask) libProviderClazz.newInstance();
                        Message message = new Message();
                        Message.obtain(null, RemoteService.REPLY, 0, 0);
                        Bundle data = new Bundle();
                        data.putByteArray("risp", task.performTask(msg.getData().getByteArray("data")));
                        message.setData(data);
                        msg.replyTo.send(message);


                        //
                        TaskDescription taskDescription = new TaskDescription();
                        taskDescription.remotableTaskClassName=msg.getData().getString("class");
                        Log.v(TAG,taskDescription.remotableTaskClassName);
                        taskDescription.data = msg.getData().getByteArray("data");
                        Thread thread = new TaskRequestThread(info.groupOwnerAddress.getHostAddress(),taskDescription,resultPointer);
                        thread.start();
                        //
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SEND_DEX:
                    Toast.makeText(getApplicationContext(), "FRAMEWORK: DEX RECEIVED", Toast.LENGTH_LONG).show();
                    // Convert array of bytes into file
                    // Before the secondary dex file can be processed by the DexClassLoader,
                    // it has to be first copied to a storage location.
                    FileOutputStream fileOuputStream = null;
                    final File dexInternalStorageFile = new File(getDir("dex", Context.MODE_PRIVATE), SECONDARY_DEX_NAME);
                    try {
                        fileOuputStream = new FileOutputStream(dexInternalStorageFile);
                        fileOuputStream.write(msg.getData().getByteArray("data"));
                        fileOuputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //
                    Intent serviceIntent = new Intent(RemoteService.this, DexTransferService.class);
                    serviceIntent.setAction(DexTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(DexTransferService.EXTRAS_FILE_PATH, Uri.fromFile(dexInternalStorageFile).toString());
                    serviceIntent.putExtra(DexTransferService.EXTRAS_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(DexTransferService.EXTRAS_PORT, Integer.valueOf(Commons.DEX_RECEIVER_PORT));
                    RemoteService.this.startService(serviceIntent);

                    Log.v(TAG,"Connecting with "+info.groupOwnerAddress.getHostAddress());

                    // Internal storage where the DexClassLoader writes the optimized dex file to.
                    final File optimizedDexOutputPath = getDir("outdex", Context.MODE_PRIVATE);
                    // Initialize the class loader with the secondary dex file.
                    cl = new DexClassLoader(dexInternalStorageFile.getAbsolutePath(),
                            optimizedDexOutputPath.getAbsolutePath(),
                            null,
                            getClassLoader());
                    Log.v(TAG,dexInternalStorageFile.getName());
                    break;
            }
        }
    }


}


