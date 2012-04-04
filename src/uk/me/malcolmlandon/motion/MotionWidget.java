package uk.me.malcolmlandon.motion;

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
		String cameraNumber = MotionWidgetConfigure.loadPrefernece(context, MotionWidgetConfigure.MOTION_WIDGET_CAMERA, mAppWidgetId);
		
		MotionCamera camera = new MotionCamera(externalUrlBase, internalUrlBase, cameraNumber, username, password);
		
		String msg = null;
		if (intent.getAction().equals(ACTION_WIDGET_STATUS)) {
			msg = camera.getStatus();
		} else if (intent.getAction().equals(ACTION_WIDGET_START)) {
			msg = camera.startDetection();
		} else if (intent.getAction().equals(ACTION_WIDGET_PAUSE)) {
			msg = camera.pauseDetection();
		} else if (intent.getAction().equals(ACTION_WIDGET_SNAPSHOT)) {
			msg = camera.snapshot();
		}			
		
		if (msg != null) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}

		super.onReceive(context, intent);
	}	
}
