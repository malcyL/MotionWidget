package uk.me.malcolmlandon.motion;
import il.me.liranfunaro.motion.HostPreferences;
import il.me.liranfunaro.motion.R;
import il.me.liranfunaro.motion.client.CameraStatus;
import il.me.liranfunaro.motion.client.MotionCameraClient;

import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.RemoteViews;


public class MotionWidget extends AppWidgetProvider {
	public static final String PREFS_NAME = MotionWidget.class.toString();
	
	public static final String PREF_WIDGET_HOST_UUID = "UUID_OF_WIDGET_";
	public static final String PREF_WIDGET_HOST_CAMERA = "CAMERA_OF_WIDGET_";

	public static String ACTION_WIDGET_STATUS = "ActionWidgetStatus";
	public static String ACTION_WIDGET_START = "ActionWidgetStart";
	public static String ACTION_WIDGET_PAUSE = "ActionWidgetPause";
	public static String ACTION_WIDGET_SNAPSHOT = "ActionWidgetSnapshot";
	public static String STATUS_TEXT_FORMAT = "%s #%s: %s";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	  RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

	  for (int id : appWidgetIds) {
		  onUpdateWidget(context,appWidgetManager,id,remoteViews);
	  }
	}
	
	public String getStatusText(HostPreferences host, MotionCameraClient camera, CameraStatus status) {
		return String.format(STATUS_TEXT_FORMAT, host.getName(), camera.getCameraNumber(), status.toString());
	}
	
	public void onUpdateWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews remoteViews) {
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
		  
		  doAsyncAction(context, ACTION_WIDGET_STATUS, appWidgetId);

		  appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
	
	protected int getAppWidgerId(Bundle extras) {
		int mAppWidgetId = -1;
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		return mAppWidgetId;
	}
	
	static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, 0);
	}
	
    static String getWidgetUUID(Context context, int appWidgetId) {
        SharedPreferences prefs = getSharedPreferences(context);
        return getWidgetUUID(prefs, appWidgetId);
    }
    
    static String getWidgetUUID(SharedPreferences prefs, int appWidgetId) {
        return prefs.getString(PREF_WIDGET_HOST_UUID + appWidgetId, "");
    }
    
    static HostPreferences getWidgetHostPreferences(Context context, int appWidgetId) {
    	String uuid = getWidgetUUID(context, appWidgetId);
        return new HostPreferences(context, uuid);
    }
    
    static HostPreferences getWidgetHostPreferences(Context context, SharedPreferences prefs, int appWidgetId) {
    	String uuid = getWidgetUUID(prefs, appWidgetId);
        return new HostPreferences(context, uuid);
    }
    
    static String getWidgetCamera(Context context, int appWidgetId) {
        SharedPreferences prefs = getSharedPreferences(context);
        return getWidgetCamera(prefs, appWidgetId);
    }
    
    static String getWidgetCamera(SharedPreferences prefs, int appWidgetId) {
        return prefs.getString(PREF_WIDGET_HOST_CAMERA + appWidgetId, "");
    }
    
    public static void setWidgetPreferences(Context context, int appWidgetId, String hostUUID, String camera) {
    	SharedPreferences prefs = getSharedPreferences(context);
    	setWidgetPreferences(prefs, appWidgetId, hostUUID, camera);
    }
    
    public static void setWidgetPreferences(SharedPreferences prefs, int appWidgetId, String hostUUID, String camera) {
    	Editor edit = prefs.edit();
    	setWidgetPreferences(edit, appWidgetId, hostUUID, camera);
    	edit.commit();
    }
    
    public static void setWidgetPreferences(Editor edit, int appWidgetId, String hostUUID, String camera) {
    	edit.putString(PREF_WIDGET_HOST_UUID + appWidgetId, hostUUID);
    	edit.putString(PREF_WIDGET_HOST_CAMERA + appWidgetId, camera);
    }
	
	public MotionCameraClient getWidgetCameraClient(Context context, int appWidgetId) {
		SharedPreferences prefs = getSharedPreferences(context);
		HostPreferences host = getWidgetHostPreferences(context, prefs, appWidgetId);
		String camera = getWidgetCamera(prefs, appWidgetId);
		return new MotionCameraClient(host, camera);
	}
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final int appWidgetId = getAppWidgerId(intent.getExtras());

		
		final String action = intent.getAction();
		
		doAsyncAction(context, action, appWidgetId);

		super.onReceive(context, intent);
	}
	
	public void doAsyncAction(final Context context,
			final String action, final int appWidgetId) {
		final MotionCameraClient camera = getWidgetCameraClient(context, appWidgetId);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final CameraStatus status = doAction(camera, action);
				if(status != null && status != CameraStatus.UNKNOWN) {
					AppWidgetManager mgr = AppWidgetManager.getInstance(context);
					
					RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.widget);
					switch(status) {
					case ACTIVE:
						break;
					case PAUSE:
						break;
					default:
					}
					
					rv.setTextViewText(R.id.status, getStatusText(getWidgetHostPreferences(context, appWidgetId), camera, status));
					String time = DateFormat.getTimeFormat(context).format(Calendar.getInstance().getTime());
					rv.setTextViewText(R.id.lastUpdate, "last update: " + time);

					mgr.updateAppWidget(appWidgetId, rv);
				}
			}
		}).start();
	}
	
	public CameraStatus doAction(MotionCameraClient camera, String action) {
		if (action.equals(ACTION_WIDGET_STATUS)) {
			return camera.getStatus();
		} else if (action.equals(ACTION_WIDGET_START)) {
			return camera.startDetection();
		} else if (action.equals(ACTION_WIDGET_PAUSE)) {
			return camera.pauseDetection();
		} else if (action.equals(ACTION_WIDGET_SNAPSHOT)) {
			camera.snapshot();
			return null;
		}
		
		return null;
	}
}
