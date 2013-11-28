package il.liranfunaro.motion;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.SearchView;

public class CameraConfigurationActivity extends GenericCameraActivity {
	
	private CameraConfigurationAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_configuration);
		
		ListView configList = (ListView) findViewById(R.id.camera_configuration_list);
		adapter = new CameraConfigurationAdapter(this, cameraClient);
		configList.setAdapter(adapter);
	}
	
	@Override
	protected void requestWindowFeatures() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera_configuration, menu);
		
		// Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView =
	            (SearchView) menu.findItem(R.id.action_search).getActionView();
	    searchView.setSearchableInfo(
	            searchManager.getSearchableInfo(getComponentName()));
	    
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
	            adapter.filter(newText);
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

		});
	    
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_write:
			new AlertDialog.Builder(this)
			.setTitle(R.string.write_configuration_title)
			.setMessage(R.string.write_configuration_text)
			.setCancelable(true)
			.setNegativeButton(android.R.string.no, null)
			.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						setProgressBarIndeterminateVisibility(true);
						
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								cameraClient.writeConfigurations();

								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										setProgressBarIndeterminateVisibility(false);
									}
								});
							}
						}).start();;
					}
				})
			.show();
			return true;
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

	@Override
    protected void onNewIntent(Intent intent) {
		Log.d(getClass().toString(), intent.toString());
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            adapter.filter(query);
        }
    }
}
