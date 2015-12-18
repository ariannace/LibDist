package info.primestar.framework;

import java.io.Serializable;

/**
 * Created by Arian Nace on 08/12/2015.
 */
public interface RemotableTask extends Serializable{
    byte[] performTask(byte[] data);
}
