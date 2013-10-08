package il.me.liranfunaro.motion.client;

public enum HostStatus {
	UNAUTHORIZED, UNAVALIBLE, AVAILIBLE, UNKNOWN;
	
	public String getUserMessage() {
		switch(this) {
		case AVAILIBLE: return "Host is availible";
		case UNAUTHORIZED: return "Username or Password is not correct";
		case UNAVALIBLE: return "Host is unavailible";
		
		case UNKNOWN:
		default:
			return "Host status is unknown";
		}
	}
}
