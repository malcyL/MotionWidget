package il.liranfunaro.motion;

import il.liranfunaro.motion.client.Host;
import il.liranfunaro.motion.client.UrlParameters;
import il.liranfunaro.motion.exceptions.HostNotExistException;

import java.net.MalformedURLException;
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
	
	public static final String PREF_PREFIX_HOSTNAME = "MotionControl_hostname_";
	
	public static final String PREF_PREFIX_EXTERNAL_PROTOCOL = "MotionControl_external_protocol_";
	public static final String PREF_PREFIX_EXTERNAL_HOST = "MotionControl_external_host_";
	public static final String PREF_PREFIX_EXTERNAL_PORT = "MotionControl_external_port_";
	
	public static final String PREF_PREFIX_INTERNAL_PROTOCOL = "MotionControl_internal_protocol_";
	public static final String PREF_PREFIX_INTERNAL_HOST = "MotionControl_internal_host_";
	public static final String PREF_PREFIX_INTERNAL_PORT = "MotionControl_internal_port_";
	
	public static final String PREF_PREFIX_USERNAME = "MotionControl_username_";
	public static final String PREF_PREFIX_PASSWORD = "MotionControl_password_";
	
	private UUID uuid;
	
	private final Context context;

	private String hostName;
	private UrlParameters externalUrl;
	private UrlParameters internalUrl;
	private String username;
	private String password;
	
	public HostPreferences(Context context) {
		if(context == null) {
			throw new IllegalArgumentException("context must not be null");
		}
		
		this.context = context;
		this.uuid = null;
	}
	
	public HostPreferences(Context context, String uuid, boolean create) throws HostNotExistException {
		if(context == null) {		
			throw new IllegalArgumentException("context must not be null");
		}
		
		this.context = context;
		
		if(uuid == null || uuid.isEmpty()) {
			if(create) {
				this.uuid = null;
				return;
			} else {
				throw new HostNotExistException("Host not exists", uuid);
			}
		} else {
			this.uuid = UUID.fromString(uuid);
		}
		
		readFromPreferences();
	}
	
	private String getStringPreference(SharedPreferences prefs, String prefix, String defaultValue) {
		return prefs.getString(prefix + uuid, defaultValue);
	}
	
	private String getStringPreference(SharedPreferences prefs, String prefix) {
		return getStringPreference(prefs, prefix, "");
	}
	
	private boolean isPreferenceExist(SharedPreferences prefs, String prefix) {
		return prefs.contains(prefix + uuid);
	}
	
	private void setStringPreference(Editor edit, String prefix, String value) {
		edit.putString(prefix + uuid, value);
	}
	
	private void removePreference(Editor edit, String prefix) {
		edit.remove(prefix + uuid);
	}
	
	public void readFromPreferences() throws HostNotExistException {
		SharedPreferences prefs = getSharedPreferences();
		
		if(!getHostsList(prefs).contains(uuid.toString())) {
			throw new HostNotExistException("Host not exists", uuid);	
		}
		
		if(!isPreferenceExist(prefs, PREF_PREFIX_EXTERNAL_HOST) || 
				!isPreferenceExist(prefs, PREF_PREFIX_EXTERNAL_PROTOCOL) ||
				!isPreferenceExist(prefs, PREF_PREFIX_EXTERNAL_PORT) ) {
			remove();
			throw new HostNotExistException("Host missing preferences. Will be deleted", uuid);	
		}
		
		this.externalUrl = new UrlParameters(getStringPreference(prefs, PREF_PREFIX_EXTERNAL_PROTOCOL),
				getStringPreference(prefs, PREF_PREFIX_EXTERNAL_HOST),
				getStringPreference(prefs, PREF_PREFIX_EXTERNAL_PORT));
		
		if(isPreferenceExist(prefs, PREF_PREFIX_INTERNAL_HOST) &&
				isPreferenceExist(prefs, PREF_PREFIX_INTERNAL_PROTOCOL) &&
				isPreferenceExist(prefs, PREF_PREFIX_INTERNAL_PORT) ) {
			this.internalUrl = new UrlParameters(getStringPreference(prefs, PREF_PREFIX_INTERNAL_PROTOCOL),
					getStringPreference(prefs, PREF_PREFIX_INTERNAL_HOST),
					getStringPreference(prefs, PREF_PREFIX_INTERNAL_PORT));
		} else {
			this.internalUrl = null;
		}
		
		this.username = getStringPreference(prefs, PREF_PREFIX_USERNAME);
		this.password = getStringPreference(prefs, PREF_PREFIX_PASSWORD);
		
		this.hostName = getStringPreference(prefs, PREF_PREFIX_HOSTNAME, "New Host");
	}
	
	public void fillActivity(Activity activity) {
		setText(activity, R.id.hostname, this.hostName);
		setText(activity, R.id.hostExternalUrl, this.externalUrl.getFullUrl());
		setText(activity, R.id.hostInternalUrl, this.internalUrl.getFullUrl());
		setText(activity, R.id.hostUsername, this.username);
		setText(activity, R.id.hostPassword, this.password);
	}
	
	public void fillFromActivity(Activity activity) throws MalformedURLException {
		this.hostName = getText(activity, R.id.hostname);
		
		String externalUrlBase = getText(activity, R.id.hostExternalUrl);
		String internalUrlBase = getText(activity, R.id.hostInternalUrl);
		
		if(externalUrlBase == null || externalUrlBase.isEmpty()) {
			throw new IllegalArgumentException("Must supply host url");
		}
		
		externalUrl = new UrlParameters(externalUrlBase);
		
		if(internalUrlBase != null && !internalUrlBase.isEmpty()) {
			internalUrl = new UrlParameters(internalUrlBase);
		} else {
			internalUrl = null;
		}
		
		if(hostName == null || hostName.isEmpty()) {
			hostName = externalUrl.getHost();
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
	
	private static void setHostList(Editor editor, Set<String> hosts) {
		editor.putStringSet(PREF_ALL_HOSTS_UUID_LIST, hosts);
	}
	
	private SharedPreferences getSharedPreferences() {
		return getSharedPreferences(context);
	}
	
	private static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public Set<String> getHostsList() {
		return getSharedPreferences().getStringSet(PREF_ALL_HOSTS_UUID_LIST, new TreeSet<String>());
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
		
		setStringPreference(edit, PREF_PREFIX_HOSTNAME, hostName);
		
		setStringPreference(edit, PREF_PREFIX_EXTERNAL_PROTOCOL, externalUrl.getProtocol());
		setStringPreference(edit, PREF_PREFIX_EXTERNAL_HOST, externalUrl.getHost());
		setStringPreference(edit, PREF_PREFIX_EXTERNAL_PORT, externalUrl.getPort());
		
		if(internalUrl != null) {
			setStringPreference(edit, PREF_PREFIX_INTERNAL_PROTOCOL, internalUrl.getProtocol());
			setStringPreference(edit, PREF_PREFIX_INTERNAL_HOST, internalUrl.getHost());
			setStringPreference(edit, PREF_PREFIX_INTERNAL_PORT, internalUrl.getPort());
		} else {
			removePreference(edit, PREF_PREFIX_INTERNAL_PROTOCOL);
			removePreference(edit, PREF_PREFIX_INTERNAL_HOST);
			removePreference(edit, PREF_PREFIX_INTERNAL_PORT);
		}
		
		setStringPreference(edit, PREF_PREFIX_USERNAME, username);
		setStringPreference(edit, PREF_PREFIX_PASSWORD, password);
		
		edit.commit();
	}
	
	public void remove() {
		if(uuid == null) {
			return;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		
		removeHostFromList(prefs, edit, uuid.toString());
		
		removePreference(edit, PREF_PREFIX_HOSTNAME);
		
		removePreference(edit, PREF_PREFIX_EXTERNAL_PROTOCOL);
		removePreference(edit, PREF_PREFIX_EXTERNAL_PROTOCOL);
		removePreference(edit, PREF_PREFIX_EXTERNAL_PROTOCOL);
		
		removePreference(edit, PREF_PREFIX_INTERNAL_PROTOCOL);
		removePreference(edit, PREF_PREFIX_INTERNAL_HOST);
		removePreference(edit, PREF_PREFIX_INTERNAL_PORT);
		
		removePreference(edit, PREF_PREFIX_USERNAME);
		removePreference(edit, PREF_PREFIX_PASSWORD);
		
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
	public UrlParameters getExternalHost() {
		return externalUrl;
	}

	@Override
	public UrlParameters getInternalHost() {
		return internalUrl;
	}

	@Override
	public String getName() {
		return hostName;
	}

	@Override
	public void setExternalHost(String externalHost) throws MalformedURLException {
		setExternalHost(new UrlParameters(externalHost));
	}

	@Override
	public void setInternalHost(String internalHost) throws MalformedURLException {
		setInternalHost(internalHost != null && !internalHost.isEmpty() ? new UrlParameters(internalHost) : null);
	}
	
	@Override
	public void setExternalHost(UrlParameters externalHost) {
		this.externalUrl = externalHost;
		
		if(hostName == null || hostName.isEmpty()) {
			hostName = externalUrl.getHost();
		}
	}

	@Override
	public void setInternalHost(UrlParameters internalHost) {
		this.internalUrl = internalHost;
	}

	@Override
	public void setName(String name) throws IllegalArgumentException {
		this.hostName = name;
		
		if(hostName == null || hostName.isEmpty()) {
			hostName = externalUrl.getHost();
		}
	}

	@Override
	public int compareTo(HostPreferences another) {
		return this.hostName.compareTo(another.hostName);
	}
}
