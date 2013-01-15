package axion.channel;

import java.util.Map;

public interface WSTemplateChannel {
	
	public boolean matches(String path);
	
	public Map<String, String> match(String path);
	
	public Object request(Object in,Map<String, String> args) throws Exception;
}
