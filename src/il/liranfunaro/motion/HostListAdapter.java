package il.liranfunaro.motion;

import il.liranfunaro.motion.client.HostStatus;
import il.liranfunaro.motion.client.MotionHostClient;
import il.liranfunaro.motion.exceptions.HostNotExistException;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class HostListAdapter extends BaseExpandableListAdapter {
	private final Context context;
	private final Activity itsActivity;
	private final int myAppWidgetId;
	
	protected HostPreferences[] hosts;
	protected MotionHostClient[] hostsClient;
	
	public HostListAdapter(Activity activity, int myAppWidgetId) {
		this.itsActivity = activity;
		this.context = itsActivity.getApplicationContext();
		this.myAppWidgetId = myAppWidgetId;
		
		updateHosts(false);
	}
	
	public void updateHosts() {
		updateHosts(true);
	}
	
	public void updateHosts(boolean notify) {
		Set<HostPreferences> hostsSet = new TreeSet<HostPreferences>();
		
		Set<String> hostsUUID = HostPreferences.getHostsList(itsActivity);
		for (String uuid : hostsUUID) {
			try {
				hostsSet.add(new HostPreferences(context, uuid, false));
			} catch (HostNotExistException e) {
				Log.e(getClass().getSimpleName(), "Missing Host", e);
			}
		}
		
		int hostCount = hostsSet.size();
		
		this.hosts = hostsSet.toArray(new HostPreferences[hostCount]);
		this.hostsClient = new MotionHostClient[hostCount];
		
		if(notify) {
			notifyDataSetChanged();
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if(hostsClient[groupPosition] == null) {
			return null;
		}

		switch(hostsClient[groupPosition].getHostStatus()) {
		case AVAILIBLE:
			return hostsClient[groupPosition].getCamera(childPosition);
		default:
			return null;
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return getCombinedChildId(groupPosition, childPosition);
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = itsActivity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.camera, null);
		}
		
		TextView item = (TextView) convertView.findViewById(R.id.cameraNumber);
		ImageButton refreshBtn = (ImageButton) convertView.findViewById(R.id.refreshCamera);
		ImageButton settingsBtn = (ImageButton) convertView.findViewById(R.id.cameraConfiguration);
		
		refreshBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hostsClient[groupPosition] = null;
				notifyDataSetChanged();
			}
		});
		
		HostStatus hostStatus = hostsClient[groupPosition].getHostStatus();
		ArrayList<String> availibleCamera = hostsClient[groupPosition].getAvalibleCameras();
		
		final String cameraNumber = availibleCamera == null ? null : availibleCamera.get(childPosition); 
		
		switch (hostStatus) {
		case UNAUTHORIZED:
		case UNAVALIBLE:
			item.setText(hostStatus.getUserMessage());
			break;
		default:
			if(cameraNumber != null) {
				item.setText("Camera #" + cameraNumber);
				
				settingsBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context, CameraConfigurationActivity.class);
						GenericCameraActivity.setIntentParameters(intent, hosts[groupPosition].getUUID(), cameraNumber);
						itsActivity.startActivity(intent);
					}
				});
				
				settingsBtn.setVisibility(View.VISIBLE);
				
			} else {
				item.setText("Loading...");
				
				settingsBtn.setVisibility(View.INVISIBLE);
			}
		}
		
		if(myAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && cameraNumber != null) {
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MotionWidget.setWidgetPreferences(context, myAppWidgetId, hosts[groupPosition].getUUID().toString(), cameraNumber);
					MotionWidget.onUpdateWidget(context, myAppWidgetId);
					
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
							myAppWidgetId);
					itsActivity.setResult(Activity.RESULT_OK, resultValue);
					itsActivity.finish();
				}
			});
			convertView.setClickable(true);
		} else {
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, MjpegActivity.class);
					GenericCameraActivity.setIntentParameters(intent, hosts[groupPosition].getUUID(), cameraNumber);
					itsActivity.startActivity(intent);
				}
			});
			convertView.setClickable(true);
//			convertView.setClickable(false);
//			convertView.setOnClickListener(null);
		}
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(hostsClient[groupPosition] == null) {
			hostsClient[groupPosition] = 
					new MotionHostClient(hosts[groupPosition], GeneralPreferences.getConnectionTimeout(context));
		}
		
		Log.i("getChildrenCount", "" + groupPosition);
		
		ArrayList<String> availibleCamera = hostsClient[groupPosition].getAvalibleCameras();
		if(availibleCamera != null) {
			return availibleCamera.size();
		}
		
		HostStatus hostStatus = hostsClient[groupPosition].getHostStatus();
		
		switch(hostStatus) {
		case UNAUTHORIZED:
		case UNAVALIBLE:
			break;
		case AVAILIBLE:
		case UNKNOWN:
		default:
			hostsClient[groupPosition].fetchAvailibleCamerasAsync(new Runnable() {
				
				@Override
				public void run() {
					itsActivity.runOnUiThread(new Runnable(){
					    public void run(){
					        notifyDataSetChanged();
					    }
					});
				}
			});
			break;
		}
		
		return 1;
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		return hosts[groupPosition];
	}

	@Override
	public int getGroupCount() {
		return hosts.length;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater infalInflater = itsActivity.getLayoutInflater();
            convertView = infalInflater.inflate(R.layout.host, null);
        }
		
		final HostPreferences host = hosts[groupPosition];
		
		final ImageButton button = (ImageButton) convertView.findViewById(R.id.editHost);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	host.edit(itsActivity);
            }
        });
		
		TextView hostNameView = (TextView) convertView.findViewById(R.id.hostName);
        hostNameView.setText(host.getName());
        
		TextView hostUrl = (TextView) convertView.findViewById(R.id.hostUrl);
		hostUrl.setText(host.getExternalHost().getHost());
		
		TextView hostUsername = (TextView) convertView.findViewById(R.id.hostUsername);
		hostUsername.setText(host.getUsername());
        
        return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		ArrayList<String> availibleCamera = hostsClient[groupPosition].getAvalibleCameras();
		return availibleCamera != null;
	}
}
