package info.primestar.transporter.threads;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import dalvik.system.DexClassLoader;
import info.primestar.framework.RemotableTask;
import info.primestar.framework.TaskDescription;
import info.primestar.transporter.Commons;
import info.primestar.transporter.RemoteService;

public class TaskReceiverThread extends Thread {
    ServerSocket serverSocket;
    Context context;

    public TaskReceiverThread(Context c){
        context = c;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Integer.valueOf(Commons.TASK_RECEIVER_PORT));
            Log.v("TaskReciverThread","listenining to "+Commons.TASK_RECEIVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                Log.v("TaskReciverThread","Connection accepted");
                TaskResponseSocketThread socketServerReplyThread
                        = new TaskResponseSocketThread(socket,context);
                socketServerReplyThread.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class TaskResponseSocketThread extends Thread {
    private Socket hostThreadSocket;
    private Context context;
    TaskResponseSocketThread(Socket socket, Context c) {
        context = c;
        hostThreadSocket = socket;
    }

    @Override
    public void run() {
        try {
            final File dexInternalStorageFile = new File(context.getFilesDir().getPath() + "/dexes/secondary.dex");
            // Internal storage where the DexClassLoader writes the optimized dex file to.
            final File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);
            // Initialize the class loader with the secondary dex file.
            DexClassLoader classLoader = new DexClassLoader(dexInternalStorageFile.getAbsolutePath(),
                    optimizedDexOutputPath.getAbsolutePath(),
                    null,
                    context.getClassLoader());
            Log.v("TaskReceiverThread","classLoader OK");



            ObjectInputStream inputstream = new ObjectInputStream(hostThreadSocket.getInputStream());
            Object object = inputstream.readObject();
            TaskDescription taskDescription = (TaskDescription) object;

            Log.v("TaskReceiverThread", "Task Received");

            Class libProviderClazz = null;
            // Load the library class from the class loader.
            libProviderClazz = classLoader.loadClass(taskDescription.remotableTaskClassName);
            // Cast the return object to the library interface so that the
            // caller can directly invoke methods in the interface.
            // Alternatively, the caller can invoke methods through reflection,
            // which is more verbose and slow.
            RemotableTask task = (RemotableTask) libProviderClazz.newInstance();

            Log.v("TaskReceiverThread", "Performing Task");
            Log.v("Data",new String(taskDescription.data));
            byte[] result = task.performTask(taskDescription.data);
            Log.v("Result",new String(result));
            hostThreadSocket.getOutputStream().write(result);
            hostThreadSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

