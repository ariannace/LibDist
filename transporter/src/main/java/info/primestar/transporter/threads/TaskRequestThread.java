package info.primestar.transporter.threads;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import info.primestar.framework.RemotableTask;
import info.primestar.framework.TaskDescription;
import info.primestar.transporter.Commons;

/**
 * Created by Arian Nace on 15/12/2015.
 */
public class TaskRequestThread extends Thread{
    private String hostAddress;
    private TaskDescription task;
    byte[] resultPointer;
    public TaskRequestThread(String hostAddress,TaskDescription task,byte[] resultPointer){
        this.hostAddress=hostAddress;
        this.task=task;
        this.resultPointer = resultPointer;
        Log.v("resultPointer:0",new String(resultPointer));
    }

    @Override
    public void run(){
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(
                    new InetSocketAddress(hostAddress, Integer.valueOf(Commons.TASK_RECEIVER_PORT)),
                    Commons.SOCKET_TIMEOUT);

            ObjectOutputStream socketOutputStream = new ObjectOutputStream(socket.getOutputStream());
            socketOutputStream.writeObject(task);
            InputStream resultStream = socket.getInputStream();

            Log.v("bytesReturned:"+resultStream.read(resultPointer),new String(resultPointer));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
