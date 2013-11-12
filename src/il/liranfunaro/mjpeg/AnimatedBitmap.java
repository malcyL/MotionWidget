package il.liranfunaro.mjpeg;

import java.io.IOException;

import android.graphics.Bitmap;

public interface AnimatedBitmap {
	public Bitmap readNextFrame() throws IOException;
}
