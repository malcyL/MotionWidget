package il.me.liranfunaro.motion;

import il.me.liranfunaro.motion.client.Host;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.Editable;
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
	
	public HostPreferences(Context context) {
		if(context == null) {
			throw new IllegalArgumentException("context and uuid must not be null");
		}
		
		this.context = context;
		this.uuid = null;
	}
	
	public static class HostNotExistException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 196979285441022733L;
		
		public HostNotExistException(String message) {
			super(message);
		}
	}
	
	public HostPreferences(Context context, String uuid) throws HostNotExistException {
		if(context == null) {
			throw new IllegalArgumentException("context and uuid must not be null");
		}
		
		this.context = context;
		if(uuid != null && !uuid.isEmpty()) {
			this.uuid = UUID.fromString(uuid);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if(!prefs.contains(PREF_PREFIX_MOTION_WIDGET_HOSTNAME + uuid)) {
				throw new HostNotExistException("No such host");
			}
			
			this.hostName = prefs.getString(PREF_PREFIX_MOTION_WIDGET_HOSTNAME + uuid, "New Host");
			
			this.externalUrl = prefs.getString(PREF_PREFIX_MOTION_WIDGET_EXTERNAL + uuid, "");
			this.internalUrl = prefs.getString(PREF_PREFIX_MOTION_WIDGET_INTERNAL + uuid, "");
			
			this.username = prefs.getString(PREF_PREFIX_MOTION_WIDGET_USERNAME + uuid, "");
			this.password = prefs.getString(PREF_PREFIX_MOTION_WIDGET_PASSWORD + uuid, "");
		} else {
			this.uuid = null;
		}
	}
	
	public void fillActivity(Activity activity) {
		setText(activity, R.id.hostname, this.hostName);
		setText(activity, R.id.hostExternalUrl, this.externalUrl);
		setText(activity, R.id.hostInternalUrl, this.internalUrl);
		setText(activity, R.id.hostUsername, this.username);
		setText(activity, R.id.hostPassword, this.password);
	}
	
	public void fillFromActivity(Activity activity) {
		this.hostName = getText(activity, R.id.hostname);
		
		String externalUrlBase = getText(activity, R.id.hostExternalUrl);
		String internalUrlBase = getText(activity, R.id.hostInternalUrl);
		
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
		
		this.username = getText(activity, R.id.hostUsername);
		this.password = getText(activity, R.id.hostPassword);
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
	
	private static void removeHostFromList(SharedPreferences prefs, Editor edit, String uuid) {
		Set<String> hosts = getHostsList(prefs);
		hosts.remove(uuid.toString());
		setHostList(edit, hosts);
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
	
	public void remove() {
		if(uuid == null) {
			return;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		
		removeHostFromList(prefs, edit, uuid.toString());
		
		edit.remove(PREF_PREFIX_MOTION_WIDGET_HOSTNAME + uuid);
		edit.remove(PREF_PREFIX_MOTION_WIDGET_EXTERNAL + uuid);
		edit.remove(PREF_PREFIX_MOTION_WIDGET_INTERNAL + uuid);
		edit.remove(PREF_PREFIX_MOTION_WIDGET_USERNAME + uuid);
		edit.remove(PREF_PREFIX_MOTION_WIDGET_PASSWORD + uuid);
		
		edit.commit();
	}
	
	public void edit(Activity activity) {
		Intent intent = new Intent(context, HostPreferencesActivity.class);
    	intent.putExtra("uuid", getUUID().toString());
    	activity.startActivityForResult(intent, MainActivity.REQUEST_ADD_EDIT_HOST);
	}
	
	private static void setText(Activity activity, int id, String text) {
		Editable edit = getEditableText(activity, id);
		edit.clear();
		edit.append(text);
	}
	
	private static String getText(Activity activity, int id) {
		return getEditableText(activity, id).toString();
	}
	
	private static Editable getEditableText(Activity activity, int id) {
		return ((EditText)activity.findViewById(id)).getText();
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
