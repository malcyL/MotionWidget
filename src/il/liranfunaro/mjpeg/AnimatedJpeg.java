package il.liranfunaro.mjpeg;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AnimatedJpeg extends MjpegInputStream implements AnimatedBitmap {

	public AnimatedJpeg(InputStream in) {
		super(in);
	}

	@Override
	public Bitmap readNextFrame() throws IOException {
	    return BitmapFactory.decodeStream(this);
	}

}
