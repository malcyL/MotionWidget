package uk.me.malcolmlandon.motion;

import il.me.liranfunaro.motion.R;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MotionWidgetConfigure extends Activity {

	private static final String PREFS_NAME = "uk.me.malcolmlandon.motion.MotionWidgetConfigure";	
	
	public static final String MOTION_WIDGET_CAMERA = "MotionWidget_camera";
	public static final String MOTION_WIDGET_PASSWORD = "MotionWidget_password";
	public static final String MOTION_WIDGET_USERNAME = "MotionWidget_username";
	public static final String MOTION_WIDGET_INTERNAL = "MotionWidget_internal";
	public static final String MOTION_WIDGET_EXTERNAL = "MotionWidget_external";
	
	private Context self = this;
	private int myAppWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			myAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, 
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		Intent cancelResultValue = new Intent();
		cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,	myAppWidgetId);
		setResult(RESULT_CANCELED, cancelResultValue);

		setContentView(R.layout.configuration);


		Button ok = (Button) findViewById(R.id.okbutton);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				String externalUrlBase = ((EditText)findViewById(R.id.ExternalText)).getText().toString();
				String internalUrlBase = ((EditText)findViewById(R.id.InternalText)).getText().toString();
				String username = ((EditText)findViewById(R.id.UsernameText)).getText().toString();
				String password = ((EditText)findViewById(R.id.PasswordText)).getText().toString();
				String camera = ((EditText)findViewById(R.id.CameraText)).getText().toString();

				SharedPreferences prefs = self.getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putString(MOTION_WIDGET_EXTERNAL+myAppWidgetId, externalUrlBase);
				edit.putString(MOTION_WIDGET_INTERNAL+myAppWidgetId, internalUrlBase);
				edit.putString(MOTION_WIDGET_USERNAME+myAppWidgetId, username);
				edit.putString(MOTION_WIDGET_PASSWORD+myAppWidgetId, password);
				edit.putString(MOTION_WIDGET_CAMERA+myAppWidgetId, camera);
				edit.commit();

				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						myAppWidgetId);
				setResult(RESULT_OK, resultValue);

				finish();
			}
		});

		Button cancel = (Button) findViewById(R.id.cancelbutton);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}		
	
    static String loadPrefernece(Context context, String key, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(key + appWidgetId, "");
    }
}
