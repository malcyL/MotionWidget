package il.me.liranfunaro.motion.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class MotionCameraClient extends MotionHostClient {
	
	protected static final String STATUS_URL_TEMPLATE = "/%s/detection/status";
	protected static final String START_URL_TEMPLATE = "/%s/detection/start";
	protected static final String PAUSE_URL_TEMPLATE = "/%s/detection/pause";
	protected static final String SNAPSHOT_URL_TEMPLATE = "/%s/action/snapshot";
	
	protected final String camera;

	public MotionCameraClient(String externalUrlBase, String internalUrlBase,
			String username, String password, String camera) {
		super(externalUrlBase, internalUrlBase, username, password);
		this.camera = camera;
	}
	
	public MotionCameraClient(MotionHostClient host, String camera) {
		super(host);
		this.camera = camera;
	}
	
	public String getCameraNumber() {
		return camera;
	}
	
	protected String getRequestURL(String template) {
		return String.format(template, camera);
	}
	
	public CameraStatus getStatus() {
		return makeCameraRequest(getRequestURL(STATUS_URL_TEMPLATE), new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream)
					throws IOException {
				Scanner streamScanner = null;
				try {
					streamScanner = new Scanner(resultStream);
					String result = streamScanner.findWithinHorizon(CameraStatus.PATTERN, 0);
	
					if (result != null) {
						try {
							return CameraStatus.valueOf(result);
						} catch (IllegalArgumentException e) {
							return CameraStatus.UNAVALIBLE;
						}
					} else {
						return CameraStatus.UNAVALIBLE;
					}
				} finally {
					if(streamScanner != null) {
						streamScanner.close();
					}
				}
			}
		});
	}

	public CameraStatus startDetection() {
		return makeCameraRequest(getRequestURL(START_URL_TEMPLATE), new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.ACTIVE;
			}
		});
	}

	public CameraStatus pauseDetection() {
		return makeCameraRequest(getRequestURL(PAUSE_URL_TEMPLATE), new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.PAUSE;
			}
		});
	}

	public CameraStatus snapshot() {
		return makeCameraRequest(getRequestURL(SNAPSHOT_URL_TEMPLATE), new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.UNKNOWN;
			}
		});
	}

}
