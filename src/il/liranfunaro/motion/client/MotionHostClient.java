package il.liranfunaro.motion.client;


import il.liranfunaro.motion.GeneralPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import android.util.Base64;

public class MotionHostClient {
	protected static final Pattern cameraLinkPattern = Pattern.compile("href=('|\")(/(\\d+)/)('|\")");
	
	protected final UrlParameters externalUrlBase;
	protected final UrlParameters internalUrlBase;
	protected final String authString;
	
	protected HostStatus hostStatus = HostStatus.UNKNOWN;
	protected ArrayList<String> availibleCameras;
	
	protected int connectionTimeoutSec = GeneralPreferences.PREF_DEFAULT_CONNECTION_TIMEOUT;
	
	public MotionHostClient(Host host, int connectionTimeout) {
		this(host.getExternalHost(),host.getInternalHost(),host.getUsername(),host.getPassword(), connectionTimeout);
	}
	
	public MotionHostClient(UrlParameters externalUrlBase, UrlParameters internalUrlBase,
			String username, String password, int connectionTimeout) {
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
		this.connectionTimeoutSec = host.connectionTimeoutSec;
	}
	
	public HostStatus getHostStatus() {
		return hostStatus;
	}
	
	public interface RequestSuccessCallback {
		Object onSuccess(InputStream resultStream) throws IOException;
	}
	
	private HttpURLConnection getUrlConnection(String requestURL) throws IOException {
		URL url = new URL(requestURL);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		if(authString != null) {
			urlConnection.setRequestProperty("Authorization", "Basic " + authString);
		}
		urlConnection.setRequestMethod("GET");
		urlConnection.setConnectTimeout(connectionTimeoutSec*1000);
		
		return urlConnection;
	}
	
	protected HostStatus getHostStatus(int statusCode) {
		switch(statusCode) {
		case 200:
			return HostStatus.AVAILIBLE;
		case 401:
			return HostStatus.UNAUTHORIZED;
		default:
			return HostStatus.UNAVALIBLE;
		}
	}
	
	protected void setHostStatus(int statusCode) {
		this.hostStatus = getHostStatus(statusCode);
	}
	
	protected Object makeSimpleRequest(String reqURL, RequestSuccessCallback action) throws IOException {
		HttpURLConnection conn = getUrlConnection(reqURL);
		try {
			conn.connect();
			setHostStatus(conn.getResponseCode());
			switch(hostStatus) {
			case AVAILIBLE:
				return action.onSuccess(conn.getInputStream());
			default:
				return null;
			}
		} finally {
			if(hostStatus.equals(HostStatus.UNKNOWN)) {
				hostStatus = HostStatus.UNAVALIBLE;
			}
			conn.disconnect();
		}
	}
	
	public Object makeRequest(String actionUrl, String actionPort, RequestSuccessCallback action) {
		try {
			try {
				return makeSimpleRequest(externalUrlBase.getFullUrl(actionPort) + actionUrl, action);
			} catch (IOException e) {
				if(internalUrlBase != null) {
					return makeSimpleRequest(internalUrlBase.getFullUrl(actionPort) + actionUrl, action);
				}
			}
		} catch (IOException e) {}
		
		return null;
	}
	
	public Object makeRequest(String actionUrl, RequestSuccessCallback action) {
		return makeRequest(actionUrl, null, action);
	}
	
	public MotionCameraClient getCamera(String camera) {
		return new MotionCameraClient(this, camera);
	}
	
	public MotionCameraClient getCamera(int camera) {
		return new MotionCameraClient(this, camera);
	}
	
	AtomicBoolean isFetching = new AtomicBoolean(false);
	
	public boolean fetchAvailibleCamerasAsync(final Runnable callback) {
		boolean success = isFetching.compareAndSet(false, true);
		if(!success) {
			return false;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				fetchAvailibleCameras();
				isFetching.set(false);
				callback.run();
			}
		}).start();
		
		return true;
	}
	
	public void fetchAvailibleCameras() {
		makeRequest("", new RequestSuccessCallback() {
			
			@Override
			public CameraStatus onSuccess(InputStream resultStream) throws IOException {
				Scanner streamScanner = null;
				try {
					streamScanner = new Scanner(resultStream);
					
					final ArrayList<String> newCameras = new ArrayList<String>();
					
					while(streamScanner.findWithinHorizon(cameraLinkPattern, 0) != null) {
						MatchResult match = streamScanner.match();
						newCameras.add(match.group(3));
					}
	
					availibleCameras = newCameras;
				} finally {
					if(streamScanner != null) {
						streamScanner.close();
					}
				}
				
				return null;
			}
		});
	}
	
	public ArrayList<String> getAvalibleCameras() {
		return availibleCameras;
	}
}
