package il.me.liranfunaro.motion;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

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
		}
		
		ExpandableListView hosts = (ExpandableListView) findViewById(R.id.hosts_list);
		adapter = new HostListAdapter(this, myAppWidgetId);
		hosts.setAdapter(adapter);
		
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
