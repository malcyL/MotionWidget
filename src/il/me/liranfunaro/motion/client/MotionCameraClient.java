package il.me.liranfunaro.motion.client;

import il.me.liranfunaro.motion.client.MotionHostClient.RequestSuccessCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class MotionCameraClient {
	
	protected static final String STATUS_URL_TEMPLATE = "/%s/detection/status";
	protected static final String START_URL_TEMPLATE = "/%s/detection/start";
	protected static final String PAUSE_URL_TEMPLATE = "/%s/detection/pause";
	protected static final String SNAPSHOT_URL_TEMPLATE = "/%s/action/snapshot";
	
	protected static final String CONFIGURATION_LIST_URL_TEMPLATE = "/%s/config/list";
	protected static final String WRITE_CONFIGURATIONS_URL_TEMPLATE = "/%s/config/writeyes";
	
	protected final String camera;
	protected final MotionHostClient hostClient;

	public MotionCameraClient(Host host, int camera, int connectionTimeout) {
		this(host, Integer.toString(camera), connectionTimeout);
	}
	
	public MotionCameraClient(Host host, String camera, int connectionTimeout) {
		this.hostClient = new MotionHostClient(host, connectionTimeout);
		this.camera = camera;
	}
	
	public MotionCameraClient(MotionHostClient host, int camera) {
		this(host, Integer.toString(camera));
	}
	
	public MotionCameraClient(MotionHostClient host, String camera) {
		this.hostClient = host;
		this.camera = camera;
	}
	
	public String getCameraNumber() {
		return camera;
	}
	
	protected String getRequestURL(String template) {
		return String.format(template, camera);
	}
	
	public HostStatus getHostStatus() {
		return hostClient.getHostStatus();
	}
	
	public MotionHostClient getHost() {
		return hostClient;
	}
	
	public CameraStatus getStatus() {
		return (CameraStatus) hostClient.makeRequest(getRequestURL(STATUS_URL_TEMPLATE), new RequestSuccessCallback() {
			
			@Override
			public Object onSuccess(InputStream resultStream) throws IOException {
				Scanner streamScanner = null;
				try {
					streamScanner = new Scanner(resultStream);
					String result = streamScanner.findWithinHorizon(CameraStatus.PATTERN, 0);
	
					if (result != null) {
						try {
							return CameraStatus.valueOf(result);
						} catch (IllegalArgumentException e) {}
					}
					
					return CameraStatus.UNKNOWN;
				} finally {
					if(streamScanner != null) {
						streamScanner.close();
					}
				}
			}
		});
	}

	public CameraStatus startDetection() {
		return (CameraStatus) hostClient.makeRequest(getRequestURL(START_URL_TEMPLATE), new RequestSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.ACTIVE;
			}
		});
	}

	public CameraStatus pauseDetection() {
		return (CameraStatus) hostClient.makeRequest(getRequestURL(PAUSE_URL_TEMPLATE), new RequestSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.PAUSE;
			}
		});
	}

	public CameraStatus snapshot() {
		return (CameraStatus) hostClient.makeRequest(getRequestURL(SNAPSHOT_URL_TEMPLATE), new RequestSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.UNKNOWN;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<CameraConfiguration> getConfigurations() {
		return (List<CameraConfiguration>) hostClient.makeRequest(getRequestURL(CONFIGURATION_LIST_URL_TEMPLATE), new RequestSuccessCallback() {
			
			@Override
			public Object onSuccess(InputStream resultStream) throws IOException {
				return CameraConfiguration.getCameraConfiguration(resultStream);
			}
		});
	}
	
	public CameraStatus writeConfigurations() {
		return (CameraStatus) hostClient.makeRequest(getRequestURL(WRITE_CONFIGURATIONS_URL_TEMPLATE), new RequestSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.UNKNOWN;
			}
		});
	}
	
	public CameraStatus updateConfiguration(CameraConfiguration conf) {
		return (CameraStatus) hostClient.makeRequest(conf.getSetAddress(), new RequestSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.UNKNOWN;
			}
		});
	}
}
