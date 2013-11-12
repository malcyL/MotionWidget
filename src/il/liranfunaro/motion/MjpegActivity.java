package il.liranfunaro.motion;

import il.liranfunaro.mjpeg.AnimatedBitmapView;
import il.liranfunaro.mjpeg.AnimatedBitmapView.AnimationStreamProducer;
import il.liranfunaro.mjpeg.AnimatedBitmapView.AnimationTask;
import il.liranfunaro.mjpeg.AnimatedJpeg;
import il.liranfunaro.motion.client.MotionHostClient.RequestSuccessCallback;

import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MjpegActivity extends GenericCameraActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mjpeg_layout);

		AnimatedBitmapView mv = (AnimatedBitmapView) findViewById(R.id.mjpegView);
		TextView fpsTextView = (TextView) findViewById(R.id.fpsTextView);

		mv.setDisplayMode(AnimatedBitmapView.SIZE_BEST_FIT);
		mv.setFpsView(fpsTextView);
		mv.showFps(true);
		mv.startPlayback(new AnimationStreamProducer() {

			@Override
			public void getAnimationStream(final AnimationTask task) {
				cameraClient.getLiveStream(new RequestSuccessCallback() {
					
					@Override
					public Object onSuccess(InputStream resultStream) throws IOException {
						task.startAnimation(new AnimatedJpeg(resultStream));
						return null;
					}
				});
			}
			
		});
	}
	
	@Override
	protected void requestWindowFeatures() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void onPause() {
		super.onPause();
		
		AnimatedBitmapView mv = (AnimatedBitmapView) findViewById(R.id.mjpegView);
		mv.stopPlayback();
	}
}
