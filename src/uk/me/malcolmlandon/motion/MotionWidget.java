package uk.me.malcolmlandon.motion;
import il.me.liranfunaro.motion.R;
import il.me.liranfunaro.motion.client.CameraStatus;
import il.me.liranfunaro.motion.client.MotionCameraClient;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RemoteViews;
import android.widget.Toast;


public class MotionWidget extends AppWidgetProvider {

	public static String ACTION_WIDGET_STATUS = "ActionWidgetStatus";
	public static String ACTION_WIDGET_START = "ActionWidgetStart";
	public static String ACTION_WIDGET_PAUSE = "ActionWidgetPause";
	public static String ACTION_WIDGET_SNAPSHOT = "ActionWidgetSnapshot";
	public static String STATUS_TEXT_FORMAT = "Camera #%s status: %s";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	  RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

	  for (int id : appWidgetIds) {
		  onUpdateWidget(context,appWidgetManager,id,remoteViews);
	  }
	}
	
	public String getStatusText(MotionCameraClient camera, CameraStatus status) {
		return String.format(STATUS_TEXT_FORMAT, camera.getCameraNumber(), status.toString());
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
		  
		  final MotionCameraClient camera = getCameraClient(context, appWidgetId);
		  
		  new Thread(new Runnable() {
				
				@Override
				public void run() {
					final CameraStatus status = camera.getStatus();
					if(status != null && status != CameraStatus.UNKNOWN) {
						remoteViews.setTextViewText(R.id.status, getStatusText(camera, status));
						appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
					}
				}
			}).start();

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
	
	public MotionCameraClient getCameraClient(Context context, int mAppWidgetId) {
		String externalUrlBase = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_EXTERNAL, mAppWidgetId);
		String internalUrlBase = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_INTERNAL, mAppWidgetId);
		String password = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_PASSWORD, mAppWidgetId);
		String username = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_USERNAME, mAppWidgetId);
		String cameraNumber = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_CAMERA, mAppWidgetId);
		
		return new MotionCameraClient(externalUrlBase, internalUrlBase, username, password, cameraNumber);
	}
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final int appWidgetId = getAppWidgerId(intent.getExtras());

		final MotionCameraClient camera = getCameraClient(context, appWidgetId);
		final String action = intent.getAction();
		final Handler toastHandler = new Handler();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final CameraStatus status = doAction(camera, action);
				if(status != null && status != CameraStatus.UNKNOWN) {
					AppWidgetManager mgr = AppWidgetManager.getInstance(context);

					RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.widget);
					rv.setTextViewText(R.id.status, getStatusText(camera, status));

					mgr.updateAppWidget(appWidgetId, rv);
				}
				
				if (status != null) {
					toastHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(context, status.getUserMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();

		super.onReceive(context, intent);
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
