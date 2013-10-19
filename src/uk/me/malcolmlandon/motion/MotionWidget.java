package uk.me.malcolmlandon.motion;

import il.me.liranfunaro.motion.GeneralPreferences;
import il.me.liranfunaro.motion.HostPreferences;
import il.me.liranfunaro.motion.R;
import il.me.liranfunaro.motion.client.CameraStatus;
import il.me.liranfunaro.motion.client.MotionCameraClient;
import il.me.liranfunaro.motion.exceptions.HostNotExistException;

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
import android.view.View;
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
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		for (int id : appWidgetIds) {
			onUpdateWidget(context, appWidgetManager, id);
		}
	}
	
	public static void onUpdateWidget(Context context, int appWidgetId) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		onUpdateWidget(context, appWidgetManager, appWidgetId);
	}

	public static void onUpdateWidget(final Context context,
			final AppWidgetManager appWidgetManager, final int appWidgetId) {
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
		snapshotIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				appWidgetId);

		PendingIntent statusPendingIntent = PendingIntent.getBroadcast(context,
				appWidgetId, statusIntent, 0);
		PendingIntent startPendingIntent = PendingIntent.getBroadcast(context,
				appWidgetId, startIntent, 0);
		PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context,
				appWidgetId, pauseIntent, 0);
		PendingIntent snapshotPendingIntent = PendingIntent.getBroadcast(
				context, appWidgetId, snapshotIntent, 0);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		
		remoteViews.setOnClickPendingIntent(R.id.button_status,
				statusPendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.button_start,
				startPendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.button_pause,
				pausePendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.button_snapshot,
				snapshotPendingIntent);

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		
		try {
			HostPreferences host = getWidgetHostPreferences(context, appWidgetId);
			doAsyncAction(context, ACTION_WIDGET_STATUS, host, appWidgetId);
		} catch (HostNotExistException e) {
			updateWidget(appWidgetManager, remoteViews, false, "Host does not exist", "error", appWidgetId);
		}
	}

	protected int getAppWidgerId(Bundle extras) {
		int mAppWidgetId = -1;
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
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

	static HostPreferences getWidgetHostPreferences(Context context,
			int appWidgetId) throws HostNotExistException {
		String uuid = getWidgetUUID(context, appWidgetId);
		return new HostPreferences(context, uuid, false);
	}

	static HostPreferences getWidgetHostPreferences(Context context,
			SharedPreferences prefs, int appWidgetId) throws HostNotExistException {
		String uuid = getWidgetUUID(prefs, appWidgetId);
		return new HostPreferences(context, uuid, false);
	}

	static String getWidgetCamera(Context context, int appWidgetId) {
		SharedPreferences prefs = getSharedPreferences(context);
		return getWidgetCamera(prefs, appWidgetId);
	}

	static String getWidgetCamera(SharedPreferences prefs, int appWidgetId) {
		return prefs.getString(PREF_WIDGET_HOST_CAMERA + appWidgetId, "");
	}

	public static void setWidgetPreferences(Context context, int appWidgetId,
			String hostUUID, String camera) {
		SharedPreferences prefs = getSharedPreferences(context);
		setWidgetPreferences(prefs, appWidgetId, hostUUID, camera);
	}

	public static void setWidgetPreferences(SharedPreferences prefs,
			int appWidgetId, String hostUUID, String camera) {
		Editor edit = prefs.edit();
		setWidgetPreferences(edit, appWidgetId, hostUUID, camera);
		edit.commit();
	}

	public static void setWidgetPreferences(Editor edit, int appWidgetId,
			String hostUUID, String camera) {
		edit.putString(PREF_WIDGET_HOST_UUID + appWidgetId, hostUUID);
		edit.putString(PREF_WIDGET_HOST_CAMERA + appWidgetId, camera);
	}
	
	public static void removeWidgetPreferences(Context context, int appWidgetId) {
		SharedPreferences prefs = getSharedPreferences(context);
		removeWidgetPreferences(prefs, appWidgetId);
	}
	
	public static void removeWidgetPreferences(SharedPreferences prefs,
			int appWidgetId) {
		Editor edit = prefs.edit();
		removeWidgetPreferences(edit, appWidgetId);
		edit.commit();
	}
	
	public static void removeWidgetPreferences(Editor edit, int appWidgetId) {
		edit.remove(PREF_WIDGET_HOST_UUID + appWidgetId);
		edit.remove(PREF_WIDGET_HOST_CAMERA + appWidgetId);
	}

	public static MotionCameraClient getWidgetCameraClient(Context context,
			int appWidgetId) throws HostNotExistException {
		SharedPreferences prefs = getSharedPreferences(context);
		HostPreferences host = getWidgetHostPreferences(context, prefs,
				appWidgetId);
		String camera = getWidgetCamera(prefs, appWidgetId);

		return new MotionCameraClient(host, camera,
				GeneralPreferences.getConnectionTimeout(context));
	}
	
	public static MotionCameraClient getWidgetCameraClient(Context context, SharedPreferences prefs,
			HostPreferences host, int appWidgetId) {
		String camera = getWidgetCamera(prefs, appWidgetId);

		return new MotionCameraClient(host, camera,
				GeneralPreferences.getConnectionTimeout(context));
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		int appWidgetId = getAppWidgerId(intent.getExtras());
		String action = intent.getAction();
		
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            removeWidgetPreferences(context, appWidgetId);
        } else {
        	SharedPreferences prefs = getSharedPreferences(context);
        	try {
				HostPreferences host = getWidgetHostPreferences(context, prefs, appWidgetId);
				doAsyncAction(context, action, host, appWidgetId);
			} catch (HostNotExistException e) {
				removeWidgetPreferences(context, appWidgetId);
			}
        }
		
		super.onReceive(context, intent);
	}
	
	public static String getStatusText(HostPreferences host,
			MotionCameraClient camera, CameraStatus status) {
		String stautsText = status == null ? "UNAVAILABLE" : status.toString();
		return String.format(STATUS_TEXT_FORMAT, host.getName(),
				camera.getCameraNumber(), stautsText);
	}

	public static void doAsyncAction(final Context context, final String action,
			final HostPreferences host,
			final int appWidgetId) {
		final RemoteViews rv = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		
		SharedPreferences prefs = getSharedPreferences(context);
		final MotionCameraClient camera = getWidgetCameraClient(context, prefs, host, appWidgetId);

		rv.setTextViewText(R.id.lastUpdate, "loading...");
		mgr.updateAppWidget(appWidgetId, rv);

		new Thread(new Runnable() {

			@Override
			public void run() {
				CameraStatus status = null;

				try {
					status = doAction(camera, action);
				} finally {
					updateWidget(context, mgr, rv, true, getStatusText(host, camera, status), appWidgetId);
				}

			}
		}).start();
	}
	
	public static void enableWidget(RemoteViews remoteViews, boolean enabled) {
		int visibility = enabled ? View.VISIBLE : View.GONE;
		
		remoteViews.setViewVisibility(R.id.button_status, visibility);
		remoteViews.setViewVisibility(R.id.button_start, visibility);
		remoteViews.setViewVisibility(R.id.button_pause, visibility);
		remoteViews.setViewVisibility(R.id.button_snapshot, visibility);
	}
	
	public static void updateWidget(Context context, AppWidgetManager appWidgetManager, 
			RemoteViews remoteViews, boolean enabled ,String statusText, int appWidgetId) {
		String time = DateFormat.getTimeFormat(context).format(
				Calendar.getInstance().getTime());
		updateWidget(appWidgetManager, remoteViews, enabled, statusText, 
				"last update: " + time, appWidgetId);
	}
	
	public static void updateWidget(AppWidgetManager appWidgetManager, 
			RemoteViews remoteViews, boolean enabled ,String statusText, String lastUpdateText, int appWidgetId) {
		remoteViews.setTextViewText(R.id.status,statusText);
		remoteViews.setTextViewText(R.id.lastUpdate, lastUpdateText);
		enableWidget(remoteViews, enabled);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	public static CameraStatus doAction(MotionCameraClient camera, String action) {
		if (action.equals(ACTION_WIDGET_STATUS)) {
			return camera.getStatus();
		} else if (action.equals(ACTION_WIDGET_START)) {
			return camera.startDetection();
		} else if (action.equals(ACTION_WIDGET_PAUSE)) {
			return camera.pauseDetection();
		} else if (action.equals(ACTION_WIDGET_SNAPSHOT)) {
			camera.snapshot();
			return camera.getStatus();
		}

		return null;
	}
}
