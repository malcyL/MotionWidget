package uk.me.malcolmlandon.motion;

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


		// the OK button
		Button ok = (Button) findViewById(R.id.okbutton);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				String externalUrlBase = ((EditText)findViewById(R.id.ExternalText)).getText().toString();
				String internalUrlBase = ((EditText)findViewById(R.id.InternalText)).getText().toString();
				String username = ((EditText)findViewById(R.id.UsernameText)).getText().toString();
				String password = ((EditText)findViewById(R.id.PasswordText)).getText().toString();
				String camera = ((EditText)findViewById(R.id.CameraText)).getText().toString();

				// save the goal date in SharedPreferences
				// we can only store simple types only like long
				// if multiple widget instances are placed
				// each can have own goal date
				// so store it under a name that contains appWidgetId
				SharedPreferences prefs = self.getSharedPreferences("prefs", 0);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("MotionWidget_external", externalUrlBase);
				edit.putString("MotionWidget_internal", internalUrlBase);
				edit.putString("MotionWidget_username", username);
				edit.putString("MotionWidget_password", password);
				edit.putString("MotionWidget_camera", camera);
				edit.commit();

				// change the result to OK
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						myAppWidgetId);
				setResult(RESULT_OK, resultValue);
				// finish closes activity
				// and sends the OK result
				// the widget will be be placed on the home screen
				finish();
			}
		});

		// cancel button
		Button cancel = (Button) findViewById(R.id.cancelbutton);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// finish sends the already configured cancel result
				// and closes activity
				finish();
			}
		});
	}		
}
