package uk.me.malcolmlandon.motion;

import il.me.liranfunaro.motion.HostPreferences;
import il.me.liranfunaro.motion.exceptions.HostNotExistException;

import java.net.MalformedURLException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MotionWidgetOldConfigure {

	public static final String PREFS_NAME = "uk.me.malcolmlandon.motion.MotionWidgetConfigure";
	
	public static final String MOTION_WIDGET_CAMERA = "MotionWidget_camera";
	public static final String MOTION_WIDGET_PASSWORD = "MotionWidget_password";
	public static final String MOTION_WIDGET_USERNAME = "MotionWidget_username";
	public static final String MOTION_WIDGET_INTERNAL = "MotionWidget_internal";
	public static final String MOTION_WIDGET_EXTERNAL = "MotionWidget_external";
	
	private final Context context;
	private final int appWidgetId;
	
	private final SharedPreferences prefs;
	
	public MotionWidgetOldConfigure(Context context, int appWidgetId) {
		this.context = context;
		this.appWidgetId = appWidgetId;
		this.prefs = context.getSharedPreferences(PREFS_NAME, 0);
	}
	
	SharedPreferences getSharedPreferences() {
        return prefs;
    }
	
    String loadPrefernece(String key) {
        return prefs.getString(key + appWidgetId, "");
    }
    
    boolean isPreferenceExist(String key) {
    	return prefs.contains(key + appWidgetId);
    }
    
    void removeOldPreferences() {
    	Editor edit = prefs.edit();
    	edit.remove(MOTION_WIDGET_EXTERNAL);
    	edit.remove(MOTION_WIDGET_INTERNAL);
    	edit.remove(MOTION_WIDGET_USERNAME);
    	edit.remove(MOTION_WIDGET_PASSWORD);
    	edit.remove(MOTION_WIDGET_CAMERA);
    	edit.commit();
    }
    
    HostPreferences createHost() {
    	if(!isPreferenceExist(MOTION_WIDGET_CAMERA)) {
    		return null;
    	}
    	
    	try {
    		HostPreferences host = new HostPreferences(context);
    		
			host.setExternalHost(loadPrefernece(MOTION_WIDGET_EXTERNAL));
			
			try {
				host.setInternalHost(loadPrefernece(MOTION_WIDGET_INTERNAL));
			} catch (MalformedURLException e) { }
			
			host.setUsername(loadPrefernece(MOTION_WIDGET_USERNAME));
			host.setPassword(loadPrefernece(MOTION_WIDGET_PASSWORD));
			
			host.commit();
			
			return host;
		} catch (MalformedURLException e) {
			return null;
		}
    }
    
    HostPreferences migratePreferences() throws HostNotExistException {
    	HostPreferences host = createHost();
    	if(host == null) {
    		throw new HostNotExistException("Host does not exist");
    	}
    	
    	MotionWidget.setWidgetPreferences(context, appWidgetId, host.getUUID().toString(), loadPrefernece(MOTION_WIDGET_CAMERA));
    	removeOldPreferences();
    	
    	return host;
    }
}
