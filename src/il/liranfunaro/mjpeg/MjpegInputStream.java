package il.liranfunaro.mjpeg;

import java.io.InputStream;

public class MjpegInputStream extends SequenceInputStream {
    public static final int[] SOI = { (int) 0xFF, (int) 0xD8 };
    public static final int[] EOF = { (int) 0xFF, (int) 0xD9 };

    public MjpegInputStream(InputStream in) {
        super(in, SOI, EOF);
    }
}