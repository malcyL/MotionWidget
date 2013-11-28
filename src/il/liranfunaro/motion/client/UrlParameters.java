package il.liranfunaro.motion.client;

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlParameters {
	public static final Pattern urlPattern = Pattern.compile("((http|https)://)?([^/:]+)(:(\\d+))?");
	
	public final String protocol;
	public final String host;
	public final String port;
	
	public UrlParameters(String protocol, String host, String port) {
		super();
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}
	
	public UrlParameters(String url) throws MalformedURLException {
		super();
		Matcher match = urlPattern.matcher(url);
		if(match.matches()) {
			this.protocol = getGroup(match, 2, "http");
			this.host = getGroup(match, 3, null);
			this.port = getGroup(match, 5, "8080");
		} else {
			throw new MalformedURLException("Malformed URL");
		}
	}
	
	public String getFullUrl() {
		return getFullUrl(null);
	}
	
	public String getFullUrl(String differentPort) {
		return protocol+"://" + host + ":" + (differentPort != null ? differentPort : port);
	}
	
	@Override
	public String toString() {
		return getFullUrl();
	}
	
	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	private String getGroup(Matcher match, int group, String defaultValue) {
		String result = match.group(group);
		if(result == null || result.isEmpty()) {
			return defaultValue;
		} else {
			return result;
		}
	}
}