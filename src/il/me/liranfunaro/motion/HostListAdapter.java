package il.me.liranfunaro.motion;

import il.me.liranfunaro.motion.client.MotionCameraClient;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class HostListAdapter extends BaseExpandableListAdapter {
	private final Context context;
	private final Activity itsActivity;
	protected HostPreferences[] hosts;

	public HostListAdapter(Activity activity) {
		this.itsActivity = activity;
		this.context = itsActivity.getApplicationContext();
		
		updateHosts();
	}
	
	public void updateHosts() {
		Set<HostPreferences> hostsSet = new TreeSet<HostPreferences>();
		
		Set<String> hostsUUID = HostPreferences.getHostsList(itsActivity);
		for (String uuid : hostsUUID) {
			hostsSet.add(new HostPreferences(context, uuid));
		}
		
		this.hosts = hostsSet.toArray(new HostPreferences[hostsSet.size()]);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		ArrayList<MotionCameraClient> cameras = hosts[groupPosition].getCameraClients();
		
		if (cameras != null) {
			return cameras.get(childPosition);
		} else {
			return null;
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return getCombinedChildId(groupPosition, childPosition);
	}

	public void updateCamera(final int hostPosition) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				hosts[hostPosition].updateCameras();
				
				itsActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}).start();
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = itsActivity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.camera, null);
		}

		Object child = getChild(groupPosition, childPosition);

		TextView item = (TextView) convertView.findViewById(R.id.cameraNumber);

		if (child == null) {
			item.setText("Loading...");
		} else {
			MotionCameraClient camera = (MotionCameraClient) child;
			item.setText(camera.getCameraNumber());
		}

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		ArrayList<MotionCameraClient> cameras = hosts[groupPosition].getCameraClients();
		
		if (cameras != null) {
			return cameras.size();
		}

		updateCamera(groupPosition);

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
            	Intent intent = new Intent(v.getContext(), AddHostActivity.class);
            	intent.putExtra("uuid", host.getUUID());
            	v.getContext().startActivity(intent);
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
