package il.liranfunaro.motion.client;

import java.net.MalformedURLException;

public interface Host {
	public String getName();
	public UrlParameters getExternalHost();
	public UrlParameters getInternalHost();
	public String getUsername();
	public String getPassword();
	
	public void setName(String name) throws IllegalArgumentException;
	public void setExternalHost(UrlParameters externalHost);
	public void setInternalHost(UrlParameters internalHost);
	public void setUsername(String username);
	public void setPassword(String password);
	
	public void setExternalHost(String externalHost) throws MalformedURLException;
	public void setInternalHost(String internalHost) throws MalformedURLException;
}
