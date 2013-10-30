package il.me.liranfunaro.motion;

import uk.me.malcolmlandon.motion.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class MainActivity extends Activity {
	public static int REQUEST_ADD_EDIT_HOST = 0x100;
	
	private HostListAdapter adapter;
	private int myAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			myAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, 
					AppWidgetManager.INVALID_APPWIDGET_ID);
			
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myAppWidgetId);
			setResult(RESULT_CANCELED, resultValue);
		}
		
		ExpandableListView hosts = (ExpandableListView) findViewById(R.id.hosts_list);
		adapter = new HostListAdapter(this, myAppWidgetId);
		hosts.setAdapter(adapter);
		registerForContextMenu(hosts);
		
		final Button button = (Button) findViewById(R.id.add_host);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(v.getContext(), HostPreferencesActivity.class);
            	startActivityForResult(intent, REQUEST_ADD_EDIT_HOST);
            }
        });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ADD_EDIT_HOST && resultCode == RESULT_OK) {
			adapter.updateHosts(true);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (v.getId() == R.id.hosts_list) {
			getMenuInflater().inflate(R.menu.host_actions, menu);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo)item.getMenuInfo();
		final HostPreferences host = (HostPreferences)adapter.getGroup((int)info.id);
		
		switch (item.getItemId()) {
		case R.id.edit_host:
			host.edit(this);
			return true;
		case R.id.remove_host:
			new AlertDialog.Builder(this)
				.setMessage(R.string.remove_host_alert)
				.setTitle(R.string.remove_host_title)
				.setCancelable(true)
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							host.remove();
							adapter.updateHosts(true);
						}
					})
				.show();
			
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	        	Intent settingIntent = new Intent(this, SettingsActivity.class);
	        	startActivity(settingIntent);
	            return true;
	        case R.id.action_about:
	        	Intent aboutIntent = new Intent(this, AboutActivity.class);
	        	startActivity(aboutIntent);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
