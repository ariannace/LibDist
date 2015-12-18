package info.primestar.transporter.threads;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import info.primestar.transporter.Commons;

public class DexReceiverThread extends Thread {
    ServerSocket serverSocket;
    private Context context;

    public DexReceiverThread(Context c){
        this.context = c;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Integer.valueOf(Commons.DEX_RECEIVER_PORT));
            Log.v("DexReciverThread","listenining to "+Commons.DEX_RECEIVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                Log.v("DexReciverThread","Connection accepted");
                SocketServerThread socketServerReplyThread
                        = new SocketServerThread(socket,context);
                socketServerReplyThread.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SocketServerThread extends Thread {
    private Socket hostThreadSocket;
    private File localCopy;
    private Context context;
    SocketServerThread(Socket socket, Context c) {
        context = c;
        hostThreadSocket = socket;
    }

    @Override
    public void run() {
        try {
            File directory = new File(context.getFilesDir().getPath() + "/dexes/");
            directory.mkdirs();
            localCopy = new File(context.getFilesDir().getPath() + "/dexes/secondary.dex");
            localCopy.createNewFile();

            InputStream inputstream = hostThreadSocket.getInputStream();
            Commons.copyFile(inputstream, new FileOutputStream(localCopy));

            hostThreadSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("DexReciverThread","DEX received");

    }
}

