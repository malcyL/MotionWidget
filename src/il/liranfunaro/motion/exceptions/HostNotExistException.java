package il.liranfunaro.motion.exceptions;

import java.util.UUID;

public class HostNotExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 196979285441022733L;
	
	private final String uuid;
	
	public HostNotExistException(String message, UUID uuid) {
		this(message, uuid.toString());
	}
	
	public HostNotExistException(String message, String uuid) {
		super(message);
		this.uuid = uuid;
	}
	
	public String getUUID() {
		return uuid;
	}
}