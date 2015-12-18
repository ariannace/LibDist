package info.primestar.sampleapp.remotable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import info.primestar.framework.RemotableTask;

/**
 * Created by Arian Nace on 08/12/2015.
 */
public class ApplyGreyscale implements RemotableTask {


    @Override
    public byte[] performTask(byte[] data) {
        Bitmap original = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
        Bitmap grayScaled = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayScaled);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(getGrayScaleMatrix()));
        canvas.drawBitmap(original, 0, 0, paint);
        original.recycle();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        grayScaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        grayScaled.recycle();
        System.gc();
        return byteArray;
    }

    private ColorMatrix getGrayScaleMatrix() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        return colorMatrix;
    }
}
