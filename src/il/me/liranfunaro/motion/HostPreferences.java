package il.me.liranfunaro.motion;

import il.me.liranfunaro.motion.R;
import il.me.liranfunaro.motion.client.CameraStatus;
import il.me.liranfunaro.motion.client.Host;
import il.me.liranfunaro.motion.client.MotionCameraClient;
import il.me.liranfunaro.motion.client.MotionHostClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.EditText;


public class HostPreferences implements Host, Comparable<HostPreferences> {
	public static final String PREF_ALL_HOSTS_UUID_LIST = "all_hosts_uuid";
	
	public static final String PREF_PREFIX_MOTION_WIDGET_HOSTNAME = "MotionControl_hostname_";
	public static final String PREF_PREFIX_MOTION_WIDGET_EXTERNAL = "MotionControl_external_";
	public static final String PREF_PREFIX_MOTION_WIDGET_INTERNAL = "MotionControl_internal_";
	public static final String PREF_PREFIX_MOTION_WIDGET_USERNAME = "MotionControl_username_";
	public static final String PREF_PREFIX_MOTION_WIDGET_PASSWORD = "MotionControl_password_";
	
	private UUID uuid;
	
	private final Context context;

	private String hostName;
	private String externalUrl;
	private String internalUrl = null;
	private String username;
	private String password;
	
	private ArrayList<MotionCameraClient> cameras = null;
	
	public HostPreferences(Context context, String uuid) {
		if(uuid == null || context == null) {
			throw new IllegalArgumentException("context and uuid must not be null");
		}
		
		this.context = context;
		this.uuid = UUID.fromString(uuid);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		this.hostName = prefs.getString(PREF_PREFIX_MOTION_WIDGET_HOSTNAME + uuid, "New Host");
		
		this.externalUrl = prefs.getString(PREF_PREFIX_MOTION_WIDGET_EXTERNAL + uuid, "");
		this.internalUrl = prefs.getString(PREF_PREFIX_MOTION_WIDGET_INTERNAL + uuid, "");
		
		this.username = prefs.getString(PREF_PREFIX_MOTION_WIDGET_USERNAME + uuid, "");
		this.password = prefs.getString(PREF_PREFIX_MOTION_WIDGET_PASSWORD + uuid, "");
	}
	
	public HostPreferences(Activity activity) throws IllegalArgumentException {
		this(activity, null);
	}
	
	public HostPreferences(Activity activity, String uuid) throws IllegalArgumentException {
		if(activity == null) {
			throw new IllegalArgumentException("activity not be null");
		}
		
		this.context = activity.getBaseContext();
		if(uuid != null) {
			this.uuid = UUID.fromString(uuid);
		} else {
			this.uuid = null;
		}
		
		this.hostName = getEditText(activity, R.id.hostname);
		
		String externalUrlBase = getEditText(activity, R.id.hostExternalUrl);
		String internalUrlBase = getEditText(activity, R.id.hostInternalUrl);
		
		if(externalUrlBase == null || externalUrlBase.isEmpty()) {
			throw new IllegalArgumentException("Must supply host url");
		}
		
		try {
			externalUrl = new UriParameters(externalUrlBase).getFullUrl();
			
			if(internalUrlBase != null && !internalUrlBase.isEmpty()) {
				internalUrl = new UriParameters(internalUrlBase).getFullUrl();
			} else {
				internalUrl = "";
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		
		if(hostName == null || hostName.isEmpty()) {
			hostName = externalUrl;
		}
		
		username = getEditText(activity, R.id.hostUsername);
		password = getEditText(activity, R.id.hostPassword);
	}
	
	public void fillFromActivity(Activity activity) {
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public static Set<String> getHostsList(Context context) {
		return getHostsList(getSharedPreferences(context));
	}
	
	public void setHostList(Set<String> hosts) {
		setHostList(context, hosts);
	}
	
	public static void setHostList(Context context, Set<String> hosts) {
		setHostList(getSharedPreferences(context), hosts);
	}
	
	public static void setHostList(SharedPreferences prefs, Set<String> hosts) {
		Editor editor = prefs.edit();
		setHostList(editor, hosts);
		editor.commit();
	}
	
	public static void setHostList(Editor editor, Set<String> hosts) {
		editor.putStringSet(PREF_ALL_HOSTS_UUID_LIST, hosts);
	}
	
	public SharedPreferences getSharedPreferences() {
		return getSharedPreferences(context);
	}
	
	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public Set<String> getHostsList() {
		return getSharedPreferences().getStringSet(PREF_ALL_HOSTS_UUID_LIST, Collections.<String> emptySet());
	}
	
	public static Set<String> getHostsList(SharedPreferences prefs) {
		return prefs.getStringSet(PREF_ALL_HOSTS_UUID_LIST, new TreeSet<String>());
	}
	
	private static UUID addHostToList(SharedPreferences prefs, Editor edit) {
		Set<String> hosts = getHostsList(prefs);
		UUID uuid;
		do {
			uuid = UUID.randomUUID();
		} while(hosts.contains(uuid.toString()));
		
		hosts.add(uuid.toString());
		setHostList(edit, hosts);
		return uuid;
	}
	
	public void commit() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		
		if(uuid == null) {
			uuid = addHostToList(prefs, edit);
		}
		
		edit.putString(PREF_PREFIX_MOTION_WIDGET_HOSTNAME + uuid, hostName);
		edit.putString(PREF_PREFIX_MOTION_WIDGET_EXTERNAL + uuid, externalUrl);
		edit.putString(PREF_PREFIX_MOTION_WIDGET_INTERNAL + uuid, internalUrl);
		edit.putString(PREF_PREFIX_MOTION_WIDGET_USERNAME + uuid, username);
		edit.putString(PREF_PREFIX_MOTION_WIDGET_PASSWORD + uuid, password);
		edit.commit();
	}
	
	public ArrayList<MotionCameraClient> getCameraClients() {
		return cameras;
	}
	
	public boolean updateCameras() {
		ArrayList<MotionCameraClient> cameras = new ArrayList<MotionCameraClient>();
		CameraStatus result = new MotionHostClient(this).getCameras(cameras);
		
		switch (result) {
		case UNAUTHORIZED:
		case UNAVALIBLE:
			return false;
		default:
			this.cameras = cameras;
			return true;
		}
	}
	
	private String getEditText(Activity activity, int id) {
		return ((EditText)activity.findViewById(id)).getText().toString();
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String getExternalHost() {
		return externalUrl;
	}

	@Override
	public String getInternalHost() {
		return internalUrl;
	}

	@Override
	public String getName() {
		return hostName;
	}

	@Override
	public void setExternalHost(String externalHost) throws MalformedURLException {
		this.externalUrl = new UriParameters(externalHost).getFullUrl();
	}

	@Override
	public void setInternalHost(String internalHost) throws MalformedURLException {
		if(internalHost != null && !internalHost.isEmpty()) {
			this.internalUrl = new UriParameters(internalHost).getFullUrl();
		} else {
			this.internalUrl = "";
		}
	}

	@Override
	public void setName(String name) throws IllegalArgumentException {
		if(name == null || name.isEmpty()) {
			throw new IllegalArgumentException("hostname must not be empty");
		}
		
		this.hostName = name;
	}

	@Override
	public int compareTo(HostPreferences another) {
		return this.hostName.compareTo(another.hostName);
	}
}
