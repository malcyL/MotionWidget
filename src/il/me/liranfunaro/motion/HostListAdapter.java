package il.me.liranfunaro.motion;

import il.me.liranfunaro.motion.client.HostStatus;
import il.me.liranfunaro.motion.client.MotionHostClient;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import uk.me.malcolmlandon.motion.MotionWidget;
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
	
	public void updateHosts(boolean notify) {
		Set<HostPreferences> hostsSet = new TreeSet<HostPreferences>();
		
		Set<String> hostsUUID = HostPreferences.getHostsList(itsActivity);
		for (String uuid : hostsUUID) {
			hostsSet.add(new HostPreferences(context, uuid));
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
			if(availibleCamera != null) {
				item.setText("Camera #" + cameraNumber);
			} else {
				item.setText("Loading...");
			}
		}
		
		if(myAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && cameraNumber != null) {
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MotionWidget.setWidgetPreferences(context, myAppWidgetId, hosts[groupPosition].getUUID().toString(), cameraNumber);
					
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
							myAppWidgetId);
					itsActivity.setResult(Activity.RESULT_OK, resultValue);
					itsActivity.finish();
				}
			});
			convertView.setClickable(true);
		} else {
			convertView.setClickable(false);
			convertView.setOnClickListener(null);
		}
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(hostsClient[groupPosition] == null) {
			hostsClient[groupPosition] = new MotionHostClient(hosts[groupPosition]);
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
            LayoutInflater infalInflater = (LayoutInflater) itsActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.host,
                    null);
        }
		
		final HostPreferences host = hosts[groupPosition];
		
		final ImageButton button = (ImageButton) convertView.findViewById(R.id.editHost);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(v.getContext(), HostPreferencesActivity.class);
            	intent.putExtra("uuid", host.getUUID().toString());
            	itsActivity.startActivityForResult(intent, MainActivity.REQUEST_ADD_EDIT_HOST);
            }
        });
		
		TextView hostNameView = (TextView) convertView.findViewById(R.id.hostName);
        hostNameView.setText(host.getName());
        
		TextView hostUrl = (TextView) convertView.findViewById(R.id.hostUrl);
		hostUrl.setText(host.getExternalHost());
		
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
		return true;
	}
}
