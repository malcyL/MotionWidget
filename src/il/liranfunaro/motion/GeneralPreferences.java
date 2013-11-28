package il.liranfunaro.motion;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class GeneralPreferences extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	public static final String PREF_CONNECTION_TIMEOUT = "connection_timeout";
	public static final int PREF_DEFAULT_CONNECTION_TIMEOUT = 30;
	public static final String PREF_DEFAULT_CONNECTION_TIMEOUT_STR = Integer.toString(PREF_DEFAULT_CONNECTION_TIMEOUT);
	
	public static int getConnectionTimeout(Context context) {
		SharedPreferences defualtPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String timeoutStr = defualtPrefs.getString(GeneralPreferences.PREF_CONNECTION_TIMEOUT, PREF_DEFAULT_CONNECTION_TIMEOUT_STR);
		try {
			return Integer.parseInt(timeoutStr);
		} catch (Exception e) {
			Log.e("IntegerError",e.getMessage(), e);
			return PREF_DEFAULT_CONNECTION_TIMEOUT;
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
        
        init();
    }
	
	public void init()
	{
		SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		Map<String,?> prefs = sharedPreferences.getAll();
		for(String key : prefs.keySet())
		{
			updateSummery(sharedPreferences, key);
		}
	}
	
	public void updateSummery(SharedPreferences sharedPreferences, String key)
	{
		Preference connectionPref = findPreference(key);
		String summery = null;
		if(connectionPref instanceof EditTextPreference || connectionPref instanceof ListPreference) {
			summery = sharedPreferences.getString(key, "");
		}
		
		if(summery != null) {
	        // Set summary to be the user-description for the selected value
	        connectionPref.setSummary(summery);
		}
	}
	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSummery(sharedPreferences, key);
    }
	
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
}
