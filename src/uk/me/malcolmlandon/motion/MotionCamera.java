package uk.me.malcolmlandon.motion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class MotionCamera {

	private static final String STATUS_URL_TEMPLATE = "%s/%s/detection/status";
	private static final String START_URL_TEMPLATE = "%s/%s/detection/start";
	private static final String PAUSE_URL_TEMPLATE = "%s/%s/detection/pause";
	private static final String SNAPSHOT_URL_TEMPLATE = "%s/%s/action/snapshot";
	
	private final String externalUrlBase;
	private final String internalUrlBase;	
	private final String camera;	
	private final HttpClient client;
	
	public MotionCamera(String externalUrlBase, String internalUrlBase, String camera, String username, String password) {
		this.externalUrlBase = externalUrlBase;
		this.internalUrlBase = internalUrlBase;
		this.camera = camera;
		
		client = getClient(username,password);
	}
	
	public String getStatus() {
		try {
			try {
				return makeStatusRequest(client, String.format(STATUS_URL_TEMPLATE, externalUrlBase,camera));
			} catch (HttpHostConnectException e) {
				return makeStatusRequest(client, String.format(STATUS_URL_TEMPLATE, internalUrlBase,camera));
			}
		} catch (Throwable t) {
			return "Unable to connect to Motion";
		}		
	}

	private String makeStatusRequest(HttpClient client, String statusUrl) throws IOException,
			ClientProtocolException {
		HttpUriRequest request = new HttpGet(statusUrl);
		HttpResponse response = client.execute(request);
		int status = response.getStatusLine().getStatusCode();
		if (status == 200) {
			return parseStatusResponse(response);
		} else if (status == 401) {
			return "Unauthorised to access Motion.";
		} else {
			return "Unable to connect to Motion";
		}
	}

	private String parseStatusResponse(HttpResponse response) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = reader.readLine();
		if (line.contains("PAUSE")) {
			return "Motion Status: PAUSED";
		} else if (line.contains("ACTIVE")) {
			return "Motion Status: ACTIVE";				
		} else {
			return "Motion status UNKNOWN. Response Body: " + line;
		}
	}
	
	public String startDetection() {
		try {
			try {
				return makeStartRequest(client, String.format(START_URL_TEMPLATE, externalUrlBase, camera));
			} catch (HttpHostConnectException e) {
				return makeStartRequest(client, String.format(START_URL_TEMPLATE, internalUrlBase, camera));
			}
		} catch (Throwable t) {
			return "Unable to connect to Motion";
		}		
	}


	private String makeStartRequest(HttpClient client, String startUrl) throws IOException,
			ClientProtocolException {
		HttpUriRequest request = new HttpGet(startUrl);
		HttpResponse response = client.execute(request);
		int status = response.getStatusLine().getStatusCode();
		if (status == 200) {
			return "Motion Detection Started";
		} else if (status == 401) {
			return "Unauthorised to access Motion.";
		} else {
			return "Detection start failed. HTTP Status: " + status;
		}
	}

	public String pauseDetection() {
		try {
			try {
				return makePauseRequest(client, String.format(PAUSE_URL_TEMPLATE, externalUrlBase, camera));
			} catch (HttpHostConnectException e) {
				return makePauseRequest(client, String.format(PAUSE_URL_TEMPLATE, internalUrlBase, camera));
			}
		} catch (Throwable t) {
			return "Unable to connect to Motion";
		}		
	}

	private String makePauseRequest(HttpClient client, String pauseUrl) throws IOException,
			ClientProtocolException {
		HttpUriRequest request = new HttpGet(pauseUrl);
		HttpResponse response = client.execute(request);
		int status = response.getStatusLine().getStatusCode();
		if (status == 200) {
			return "Motion Detection Paused";
		} else if (status == 401) {
			return "Unauthorised to access Motion.";
		} else {
			return "Detection pause failed. HTTP Status: " + status;
		}
	}

	public String snapshot() {
		try {
			try {
				return makeSnapshotRequest(client, String.format(SNAPSHOT_URL_TEMPLATE, externalUrlBase, camera));
			} catch (HttpHostConnectException e) {
				return makeSnapshotRequest(client, String.format(SNAPSHOT_URL_TEMPLATE, internalUrlBase, camera));
			}
		} catch (Throwable t) {
			return "Unable to connect to Motion";
		}		
	}

	private String makeSnapshotRequest(HttpClient client, String pauseUrl) throws IOException,
			ClientProtocolException {
		HttpUriRequest request = new HttpGet(pauseUrl);
		HttpResponse response = client.execute(request);
		int status = response.getStatusLine().getStatusCode();
		if (status == 200) {
			return "Snapshot Taken";
		} else if (status == 401) {
			return "Unauthorised to access Motion.";
		} else {
			return "Snapshot failed. HTTP Status: " + status;
		}
	}

	private DefaultHttpClient getClient(String username, String password) {
        DefaultHttpClient ret = null;

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
        ret = new DefaultHttpClient(manager, params);
		Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
		ret.getCredentialsProvider().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), defaultcreds);
        return ret;
    }	
	
}
