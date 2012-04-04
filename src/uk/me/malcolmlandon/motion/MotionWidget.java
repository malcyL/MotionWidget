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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MotionWidget extends AppWidgetProvider {

	public static String ACTION_WIDGET_STATUS = "ActionWidgetStatus";
	public static String ACTION_WIDGET_START = "ActionWidgetStart";
	public static String ACTION_WIDGET_PAUSE = "ActionWidgetPause";
	public static String ACTION_WIDGET_SNAPSHOT = "ActionWidgetSnapshot";

	private static final String STATUS_URL = "%s/%s/detection/status";
	private static final String START_URL = "%s/%s/detection/start";
	private static final String PAUSE_URL = "%s/%s/detection/pause";
	private static final String SNAPSHOT_URL = "%s/%s/action/snapshot";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	  RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

	  int numWidgets = appWidgetIds.length;
	  for (int i=0; i<numWidgets; i++) {
		  int appWidgetId = appWidgetIds[i];

		  Intent statusIntent = new Intent(context, MotionWidget.class);
		  statusIntent.setAction(ACTION_WIDGET_STATUS);
		  statusIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		  Intent startIntent = new Intent(context, MotionWidget.class);
		  startIntent.setAction(ACTION_WIDGET_START);
		  startIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		  Intent pauseIntent = new Intent(context, MotionWidget.class);
		  pauseIntent.setAction(ACTION_WIDGET_PAUSE);
		  pauseIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		  Intent snapshotIntent = new Intent(context, MotionWidget.class);
		  snapshotIntent.setAction(ACTION_WIDGET_SNAPSHOT);
		  snapshotIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		  PendingIntent statusPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, statusIntent, 0);
		  PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, startIntent, 0);
		  PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, pauseIntent, 0);
		  PendingIntent snapshotPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, snapshotIntent, 0);
		  
		  remoteViews.setOnClickPendingIntent(R.id.button_status, statusPendingIntent);
		  remoteViews.setOnClickPendingIntent(R.id.button_start, startPendingIntent);
		  remoteViews.setOnClickPendingIntent(R.id.button_pause, pausePendingIntent);
		  remoteViews.setOnClickPendingIntent(R.id.button_snapshot, snapshotPendingIntent);

		  appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	  }
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle extras = intent.getExtras();
		int mAppWidgetId = -1;
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		String externalUrlBase = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_EXTERNAL, mAppWidgetId);
		String internalUrlBase = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_INTERNAL, mAppWidgetId);
		String password = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_PASSWORD, mAppWidgetId);
		String username = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_USERNAME, mAppWidgetId);
		String camera = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_CAMERA, mAppWidgetId);
		
		HttpClient client = getClient(username,password);
		
		String msg = null;
		if (intent.getAction().equals(ACTION_WIDGET_STATUS)) {
			msg = getStatus(client, externalUrlBase, internalUrlBase, camera);
		}			
		if (intent.getAction().equals(ACTION_WIDGET_START)) {
			msg = startDetection(client, externalUrlBase, internalUrlBase, camera);
		}			
		if (intent.getAction().equals(ACTION_WIDGET_PAUSE)) {
			msg = pauseDetection(client, externalUrlBase, internalUrlBase, camera);
		}			
		if (intent.getAction().equals(ACTION_WIDGET_SNAPSHOT)) {
			msg = snapshot(client, externalUrlBase, internalUrlBase, camera);
		}			
		
		if (msg != null) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}

		super.onReceive(context, intent);
	}

	private String getStatus(HttpClient client, String externalUrlBase, String internalUrlBase, String camera) {
		try {
			try {
				return makeStatusRequest(client, String.format(STATUS_URL, externalUrlBase,camera));
			} catch (HttpHostConnectException e) {
				return makeStatusRequest(client, String.format(STATUS_URL, internalUrlBase,camera));
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
	
	private String startDetection(HttpClient client, String externalUrlBase, String internalUrlBase, String camera) {
		try {
			try {
				return makeStartRequest(client, String.format(START_URL, externalUrlBase, camera));
			} catch (HttpHostConnectException e) {
				return makeStartRequest(client, String.format(START_URL, internalUrlBase, camera));
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

	private String pauseDetection(HttpClient client, String externalUrlBase, String internalUrlBase, String camera) {
		try {
			try {
				return makePauseRequest(client, String.format(PAUSE_URL, externalUrlBase, camera));
			} catch (HttpHostConnectException e) {
				return makePauseRequest(client, String.format(PAUSE_URL, internalUrlBase, camera));
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

	private String snapshot(HttpClient client, String externalUrlBase, String internalUrlBase, String camera) {
		try {
			try {
				return makeSnapshotRequest(client, String.format(SNAPSHOT_URL, externalUrlBase, camera));
			} catch (HttpHostConnectException e) {
				return makeSnapshotRequest(client, String.format(SNAPSHOT_URL, internalUrlBase, camera));
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
