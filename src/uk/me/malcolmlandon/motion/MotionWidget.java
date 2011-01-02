package uk.me.malcolmlandon.motion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MotionWidget extends AppWidgetProvider {

	public static String ACTION_WIDGET_STATUS = "ActionWidgetStatus";
	public static String ACTION_WIDGET_START = "ActionWidgetStart";
	public static String ACTION_WIDGET_PAUSE = "ActionWidgetPause";

	private static final String STATUS_URL = "/0/detection/status";
	private static final String START_URL = "/0/detection/start";
	private static final String PAUSE_URL = "/0/detection/pause";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	  RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

	  Intent statusIntent = new Intent(context, MotionWidget.class);
	  statusIntent.setAction(ACTION_WIDGET_STATUS);

	  Intent startIntent = new Intent(context, MotionWidget.class);
	  startIntent.setAction(ACTION_WIDGET_START);

	  Intent pauseIntent = new Intent(context, MotionWidget.class);
	  pauseIntent.setAction(ACTION_WIDGET_PAUSE);

	  PendingIntent statusPendingIntent = PendingIntent.getBroadcast(context, 0, statusIntent, 0);
	  PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, 0);
	  PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, 0);
	  
	  remoteViews.setOnClickPendingIntent(R.id.button_status, statusPendingIntent);
	  remoteViews.setOnClickPendingIntent(R.id.button_start, startPendingIntent);
	  remoteViews.setOnClickPendingIntent(R.id.button_pause, pausePendingIntent);

	  appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

	}
	
	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
		String externalUrlBase = "";
		String internalUrlBase = "";
		String username = "";
		String password = "";
		if (prefs != null) {
			externalUrlBase = prefs.getString("MotionWidget_external", "external url base not found");
			internalUrlBase = prefs.getString("MotionWidget_internal", "internal url base not found");
	        password = prefs.getString("MotionWidget_password", "password not found");
	        username = prefs.getString("MotionWidget_username", "username not found");
		}
		
		HttpClient client = getClient(username,password);
		
		String msg = null;
		if (intent.getAction().equals(ACTION_WIDGET_STATUS)) {
			msg = getStatus(client, externalUrlBase, internalUrlBase);
		}			
		if (intent.getAction().equals(ACTION_WIDGET_START)) {
			msg = startDetection(client, externalUrlBase, internalUrlBase);
		}			
		if (intent.getAction().equals(ACTION_WIDGET_PAUSE)) {
			msg = pauseDetection(client, externalUrlBase, internalUrlBase);
		}			
		
		if (msg != null) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}

		super.onReceive(context, intent);
	}

	private String getStatus(HttpClient client, String externalUrlBase, String internalUrlBase) {
		try {
			try {
				return makeStatusRequest(client, externalUrlBase + STATUS_URL);
			} catch (HttpHostConnectException e) {
				return makeStatusRequest(client, internalUrlBase + STATUS_URL);
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
	
	private String startDetection(HttpClient client, String externalUrlBase, String internalUrlBase) {
		try {
			try {
				return makeStartRequest(client, externalUrlBase + START_URL);
			} catch (HttpHostConnectException e) {
				return makeStartRequest(client, internalUrlBase + START_URL);
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

	private String pauseDetection(HttpClient client, String externalUrlBase, String internalUrlBase) {
		try {
			try {
				return makePauseRequest(client, externalUrlBase + PAUSE_URL);
			} catch (HttpHostConnectException e) {
				return makePauseRequest(client, internalUrlBase + PAUSE_URL);
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

	public DefaultHttpClient getClient(String username, String password) {
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
