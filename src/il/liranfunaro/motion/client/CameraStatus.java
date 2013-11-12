package il.liranfunaro.motion.client;

import java.util.regex.Pattern;

public enum CameraStatus {
	ACTIVE,PAUSE,UNKNOWN;
	
	public static final Pattern PATTERN = CameraStatus.getPattern();
	
	public static Pattern getPattern() {
		StringBuilder result = new StringBuilder();
		
		boolean isFirst = true;
		result.append('(');
		
		for(CameraStatus status : CameraStatus.values()) {
			if(!isFirst) {
				result.append('|');
			}
			isFirst = false;
			result.append(status.toString());
		}
		result.append(')');
		
		return Pattern.compile(result.toString());
	}
}