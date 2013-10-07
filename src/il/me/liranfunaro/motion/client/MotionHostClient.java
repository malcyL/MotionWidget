package il.me.liranfunaro.motion.client;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import android.util.Base64;

public class MotionHostClient {
	protected static final Pattern cameraLinkPattern = Pattern.compile("href=('|\")(/(\\d+)/)('|\")");
	
	protected final String externalUrlBase;
	protected final String internalUrlBase;
	protected final String authString;
	
	public MotionHostClient(Host host) {
		this(host.getExternalHost(),host.getInternalHost(),host.getUsername(),host.getPassword());
	}
	
	public MotionHostClient(String externalUrlBase, String internalUrlBase,
			String username, String password) {
		this.externalUrlBase = externalUrlBase;
		this.internalUrlBase = internalUrlBase;
		
		if(username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
			this.authString = Base64.encodeToString((username + ":" + password).getBytes(), Base64.URL_SAFE);
		} else {
			this.authString = null;
		}
	}
	
	public MotionHostClient(MotionHostClient host) {
		this.externalUrlBase = host.externalUrlBase;
		this.internalUrlBase = host.internalUrlBase;
		this.authString = host.authString;
	}
	
	
	protected interface CameraSuccessCallback {
		CameraStatus onSuccess(InputStream resultStream) throws IOException;
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
	
	protected CameraStatus makeSimpleCameraRequest(String reqURL, CameraSuccessCallback action) throws IOException {
		HttpURLConnection conn = getUrlConnection(reqURL);
		try {
			conn.connect();
			switch(conn.getResponseCode()) {
			case 200:
				return action.onSuccess(conn.getInputStream());
			case 401:
				return CameraStatus.UNAUTHORIZED;
			default:
				throw new IOException("Server Unavailable");
			}
		} finally {
			conn.disconnect();
		}
	}
	
	protected CameraStatus makeCameraRequest(String actionUrl, CameraSuccessCallback action) {
		try {
			try {
				return makeSimpleCameraRequest(externalUrlBase + actionUrl, action);
			} catch (IOException e) {
				return makeSimpleCameraRequest(internalUrlBase + actionUrl, action);
			}
		} catch (IOException e) {
			return CameraStatus.UNAVALIBLE;
		}
	}
	
	public MotionCameraClient getCamera(String camera) {
		return new MotionCameraClient(this, camera);
	}
	
	public CameraStatus getCameras(final ArrayList<MotionCameraClient> cameras) {
		
		return makeCameraRequest("", new CameraSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				Scanner streamScanner = null;
				try {
					streamScanner = new Scanner(resultStream);
					while(streamScanner.findWithinHorizon(cameraLinkPattern, 0) != null) {
						MatchResult match = streamScanner.match();
						cameras.add(getCamera(match.group(3)));
					}
	
				} finally {
					if(streamScanner != null) {
						streamScanner.close();
					}
				}
				
				return CameraStatus.UNKNOWN;
			}
		});
	}
}
