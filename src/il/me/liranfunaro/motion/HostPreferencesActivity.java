package il.me.liranfunaro.motion;


import il.me.liranfunaro.motion.exceptions.HostNotExistException;

import java.net.MalformedURLException;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HostPreferencesActivity extends Activity {
	private String uuid = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_preferences);
		// Show the Up button in the action bar.
		setupActionBar();
		
		uuid = getIntent().getStringExtra("uuid");
		
		if(uuid != null && !uuid.isEmpty()) {
			try {
				HostPreferences host = new HostPreferences(this, uuid, false);
				host.fillActivity(this);
			} catch (HostNotExistException e) {
				finish();
			}
		}
		
		EditText externalUri = (EditText) findViewById(R.id.hostExternalUrl);
		EditText internalUri = (EditText) findViewById(R.id.hostInternalUrl);
		
		externalUri.setOnFocusChangeListener (new UriInputNormalizer(true));
		internalUri.setOnFocusChangeListener (new UriInputNormalizer(false));
		
		Button cancel = (Button) findViewById(R.id.cancelbutton);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		Button ok = (Button) findViewById(R.id.okbutton);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					HostPreferences host = new HostPreferences(HostPreferencesActivity.this, uuid, true);
					host.fillFromActivity(HostPreferencesActivity.this);
					host.commit();
					setResult(RESULT_OK);
					finish();
				} catch (HostNotExistException e) {
					Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_host, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class UriInputNormalizer implements TextView.OnFocusChangeListener {
		private final boolean isMendatory;
		
		public UriInputNormalizer(boolean isMendatory) {
			this.isMendatory = isMendatory;
		}
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) return;
			Editable input = ((EditText)v).getText();
			String currentText = input.toString();
			
			if(currentText.isEmpty()) {
				setViewState(v,!isMendatory);
				return;
			}
			
			try {
				UriParameters url = new UriParameters(currentText);
				input.clear();
				input.append(url.toString());
				setViewState(v,true);
			} catch (MalformedURLException e) {
				setViewState(v,false);
				Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
		
		@SuppressWarnings("deprecation")
		public void setViewState(View v, boolean valid) {
			if(valid) {
				v.setBackgroundDrawable(null);
			} else {
				v.setBackgroundColor(v.getResources().getColor(R.color.background_wrong_uri));
			}
		}
	}
}
