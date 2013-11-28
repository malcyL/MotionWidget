package il.liranfunaro.motion;

import il.liranfunaro.motion.client.CameraConfiguration;
import il.liranfunaro.motion.client.CameraStatus;
import il.liranfunaro.motion.client.MotionCameraClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CameraConfigurationAdapter extends BaseAdapter {
	
	private final Activity itsActivity;
	private final MotionCameraClient camera;
	private CameraConfiguration[] configurations = null;
	private int configCount = 0;
	
	private Set<Map.Entry<String, CameraConfiguration>> allConfigurationsEntrySet = null;
	
	private boolean[] updating = null;
	
	CameraConfigurationAdapter(Activity itsActivity, MotionCameraClient camera) {
		this.itsActivity = itsActivity;
		this.camera = camera;
		getConfigurations();
	}
	
	private void setConfigArray() {
		configCount = allConfigurationsEntrySet.size();
		
		if(configurations == null) {
			configurations = new CameraConfiguration[configCount];
		} 
		
		int i = 0;
		
		for (Map.Entry<String, CameraConfiguration> entry : allConfigurationsEntrySet) {
			configurations[i++] = entry.getValue();
		}
	}
	
	private void getConfigurations() {
		
		new AsyncTask<Void, Void, HashMap<String, CameraConfiguration>>() {

			@Override
			protected HashMap<String, CameraConfiguration> doInBackground(Void... params) {
				return camera.getConfigurations();
			}
			
			@Override
			protected void onPostExecute(HashMap<String, CameraConfiguration> result) {
				allConfigurationsEntrySet = result.entrySet();
				setConfigArray();
				
				// Automatically set to false
				updating = new boolean[configurations.length];
				notifyDataSetChanged();
			}
			
			
		}.execute((Void)null);
	}
	
	public void filter(String pattern) {
		if(pattern == null || pattern.isEmpty()) {
			setConfigArray();
		} else {
			Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			configCount = 0;
			
			for (Map.Entry<String, CameraConfiguration> entry : allConfigurationsEntrySet) {
				if(regex.matcher(entry.getKey()).find() || regex.matcher(entry.getValue().getValue()).find()) {
					configurations[configCount++] = entry.getValue();
				}
			}
		}
		
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if(configurations == null) {
			return 1;
		} else {
			return configCount;
		}
	}

	@Override
	public Object getItem(int position) {
		if(configurations == null) {
			return null;
		} else {
			return configurations[position];
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater infalInflater = itsActivity.getLayoutInflater();
            convertView = infalInflater.inflate(R.layout.camera_configuration, null);
        }

		TextView name = (TextView) convertView.findViewById(R.id.configuration_name);
		TextView value = (TextView) convertView.findViewById(R.id.configuration_value);
		ProgressBar loading = (ProgressBar) convertView.findViewById(R.id.updateProgress);
		
		final CameraConfiguration conf = (CameraConfiguration) getItem(position);
		
		if(conf != null) {
			name.setText(conf.getName());
			value.setText(conf.getValue());
			
			if(!updating[position]) {
				loading.setVisibility(View.GONE);
				
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(itsActivity);
						
						final EditText input = new EditText(itsActivity);
						input.setInputType(InputType.TYPE_CLASS_TEXT | 
								InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
						input.setText(conf.getValue());
						builder.setView(input);
						
						builder
							.setTitle(R.string.edit_configuration_title)
							.setMessage(conf.getName())
							.setCancelable(true)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int id) {
											
											new AsyncTask<Void, Void, CameraStatus>() {

												@Override
												protected void onPreExecute() {
													updating[position] = true;
													notifyDataSetChanged();
													conf.setValue(input.getText().toString());
												}
												
												@Override
												protected CameraStatus doInBackground(Void... params) {
													return camera.updateConfiguration(conf);
												}
												
												@Override
												protected void onPostExecute(CameraStatus result) {
													updating[position] = false;
													notifyDataSetChanged();
												}
											}.execute((Void)null);
										}
									});
						builder.setNegativeButton(android.R.string.cancel, null);
	
						builder.show();
					}
				});
			} else {
				loading.setVisibility(View.VISIBLE);
				convertView.setOnClickListener(null);
			}
		} else {
			name.setText("Loading...");
			value.setText("");
			convertView.setOnClickListener(null);
		}
		
        return convertView;
	}

}
