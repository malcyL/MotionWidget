package il.liranfunaro.motion;

import il.liranfunaro.motion.client.MotionCameraClient;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GenericCameraActivity extends Activity {
	
	public static final String EXTRA_HOST_UUID = "host_uuid";
	public static final String EXTRA_CAMERA_NUMBER = "camera_number";
	
	public static void setIntentParameters(Intent intent, UUID hostUUID, String camera) {
		setIntentParameters(intent, hostUUID.toString(), camera);
	}
	
	public static void setIntentParameters(Intent intent, String hostUUID, String camera) {
		intent.putExtra(EXTRA_HOST_UUID, hostUUID);
		intent.putExtra(EXTRA_CAMERA_NUMBER, camera);
	}
	
	protected MotionCameraClient cameraClient = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeatures();
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
		}
		
		String hostUUID = extras.getString(EXTRA_HOST_UUID);
		String camera = extras.getString(EXTRA_CAMERA_NUMBER);
		
		if(hostUUID == null || camera == null) {
			finish();
		}
		
		try {
			HostPreferences host = new HostPreferences(this, hostUUID, false);
			cameraClient = new MotionCameraClient(host, camera, GeneralPreferences.getConnectionTimeout(this));
		} catch (Exception e) {
			finish();
		}
	}
	
	protected void requestWindowFeatures() {
		
	}
}
