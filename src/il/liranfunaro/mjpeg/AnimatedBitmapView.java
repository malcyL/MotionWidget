package il.liranfunaro.mjpeg;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class AnimatedBitmapView extends SurfaceView implements SurfaceHolder.Callback {
	static final String TAG = "AnimatedBitmapView";

	public final static int SIZE_STANDARD = 1;
	public final static int SIZE_BEST_FIT = 4;
	public final static int SIZE_FULLSCREEN = 8;

	private AnimationTask task = new AnimationTask();
	boolean showFps = false;
	private final AtomicBoolean playing = new AtomicBoolean(false);

	Paint framePaint = new Paint();
	TextView fpsTextView = null;
	
	int dispWidth;
	int dispHeight;
	int displayMode;
	
	SurfaceHolder holder;
	
	private int frameCounter = 0;
	private long start = 0;
	
	public void setFpsView(TextView textView) {
		this.fpsTextView = textView;
	}
	
	public AnimatedBitmapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public AnimatedBitmapView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		holder = getHolder();
		holder.addCallback(this);

		setFocusable(true);
		
		dispWidth = getWidth();
		dispHeight = getHeight();
	}

	public void startPlayback(AnimationStreamProducer producer) {
		if(playing.compareAndSet(false, true)) {
			if(producer != null) {
				task.execute(producer);
			} else {
				task.execute();
			}
		}
	}
	
	public void startPlayback() {
		startPlayback(null);
	}

	public void stopPlayback() {
		if(playing.compareAndSet(true, false)) {
			while (true) {
				try {
					task.get();
					return;
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}		
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startPlayback();
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopPlayback();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
		synchronized (holder) {
			dispWidth = w;
			dispHeight = h;
		}
	}

	public void setFrame(Bitmap bitmap) {
		synchronized (holder) {
			Canvas canvas = holder.lockCanvas();
			
			try {
				Rect destRect = calculateDestinationRect(bitmap);
				
				//canvas.drawColor(Color.RED);
				canvas.drawBitmap(bitmap, null, destRect, framePaint);
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	public void showFps(boolean b) {
		showFps = b;
	}

	public void setDisplayMode(int s) {
		displayMode = s;
	}
	
	private Rect calculateDestinationRect(Bitmap bitmap) {
		return calculateDestinationRect(bitmap.getWidth(), bitmap.getHeight());
	}

	private Rect calculateDestinationRect(int bitmapWidth, int bitmapHeight) {
		switch(displayMode) {
		case AnimatedBitmapView.SIZE_BEST_FIT:
			float bitmapAspectRation = (float) bitmapWidth / (float) bitmapHeight;
			
			bitmapWidth = dispWidth;
			bitmapHeight = (int) (dispWidth / bitmapAspectRation);
			
			if (bitmapHeight > dispHeight) {
				bitmapHeight = dispHeight;
				bitmapWidth = (int) (dispHeight * bitmapAspectRation);
			}
			
		case AnimatedBitmapView.SIZE_STANDARD:
			int left = (dispWidth / 2) - (bitmapWidth / 2);
			int top = (dispHeight / 2) - (bitmapHeight / 2);
			return new Rect(left, top, left + bitmapWidth, top + bitmapHeight);
			
		case AnimatedBitmapView.SIZE_FULLSCREEN:
			return new Rect(0, 0, dispWidth, dispHeight);
		}
		
		return null;
	}
	
	public interface AnimationStreamProducer {
		public void getAnimationStream(AnimationTask task);
	}
	
	public class AnimationTask extends AsyncTask<AnimationStreamProducer, Integer, Void> {
		public AnimationStreamProducer producer = null;
		
		public void startAnimation(AnimatedBitmap animatedBitmap) throws IOException {
			while (playing.get()) {
				setFrame(animatedBitmap.readNextFrame());
				
				if (showFps) {
					++frameCounter;
					
					if ((System.currentTimeMillis() - start) >= 1000) {
						publishProgress(frameCounter);
						frameCounter = 0;
						start = System.currentTimeMillis();
					}
				}
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			String fps = String.valueOf(progress[0]) + " fps";

			if(fpsTextView != null) {
				fpsTextView.setText(fps);
			}
	     }
		
		@Override
		protected Void doInBackground(AnimationStreamProducer... producer) {
			if(producer.length == 0 || producer[0] == null) {
				if(this.producer == null) {
					return null;
				}
			} else {
				this.producer = producer[0];
			}
			
			this.producer.getAnimationStream(this);
			
			return null;
		}
	}
}