package il.me.liranfunaro.motion.client;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

import android.util.Base64;

public class MotionClient {

	private static final String STATUS_URL_TEMPLATE = "%s/%s/detection/status";
	private static final String START_URL_TEMPLATE = "%s/%s/detection/start";
	private static final String PAUSE_URL_TEMPLATE = "%s/%s/detection/pause";
	private static final String SNAPSHOT_URL_TEMPLATE = "%s/%s/action/snapshot";
	
	public static final Pattern statusPattern = Pattern.compile("(ACTIVE|PAUSED)");
	
	private final String externalUrlBase;
	private final String internalUrlBase;
	private final String authString;
	private final String camera;
	
	public MotionClient(String externalUrlBase, String internalUrlBase,
			String camera, String username, String password) {
		this.externalUrlBase = externalUrlBase;
		this.internalUrlBase = internalUrlBase;
		
		if(username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
			this.authString = Base64.encodeToString((username + ":" + password).getBytes(), Base64.URL_SAFE);
		} else {
			this.authString = null;
		}
		
		this.camera = camera;
	}
	
	private interface CameraSuccessCallback {
		CameraStatus onSuccess(InputStream resultStream) throws IOException;
	}
	
	public String getCameraNumber() {
		return camera;
	}
	
	public String getRequestURL(String template, boolean external) {
		return String.format(template, external ? externalUrlBase : internalUrlBase, camera);
	}
	
	private HttpURLConnection getUrlConnection(String requestURL) throws IOException {
		URL url = new URL(requestURL);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		if(authString != null) {
			urlConnection.setRequestProperty("Authorization", "Basic " + authString);
		}
		urlConnection.setRequestMethod("GET");
		return urlConnection;
	}
	
	private CameraStatus makeSimpleCameraRequest(String reqURL, CameraSuccessCallback action) throws IOException {
		HttpURLConnection conn = getUrlConnection(reqURL);
		try {
			conn.connect();
			switch(conn.getResponseCode()) {
			case 200:
				return action.onSuccess(conn.getInputStream());
			case 401:
				return CameraStatus.UNAUTHORIZED;
			default:
				throw new IOException("Server unavilibe");
			}
		} finally {
			conn.disconnect();
		}
	}
	
	private CameraStatus makeCameraRequest(String actionTemplate, CameraSuccessCallback action) {
		try {
			try {
				return makeSimpleCameraRequest(getRequestURL(actionTemplate, true), action);
			} catch (IOException e) {
				return makeSimpleCameraRequest(getRequestURL(actionTemplate, false), action);
			}
		} catch (IOException e) {
			return CameraStatus.UNAVALIBLE;
		}
	}

	public CameraStatus getStatus() {
		return makeCameraRequest(STATUS_URL_TEMPLATE, new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream)
					throws IOException {
				Scanner streamScanner = null;
				try {
					streamScanner = new Scanner(resultStream);
					String result = streamScanner.findWithinHorizon(statusPattern,
							0);
	
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
		return makeCameraRequest(START_URL_TEMPLATE, new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.ACTIVE;
			}
		});
	}

	public CameraStatus pauseDetection() {
		return makeCameraRequest(PAUSE_URL_TEMPLATE, new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.ACTIVE;
			}
		});
	}

	public CameraStatus snapshot() {
		return makeCameraRequest(SNAPSHOT_URL_TEMPLATE, new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				return CameraStatus.UNKNOWN;
			}
		});
		
//		switch(result) {
//		case UNKNOWN: return "Snapshot taken";
//		default: return result.getUserMessage();
//		}
	}
}
