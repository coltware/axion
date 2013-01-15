package axion.channel;

import java.util.Map;

import axion.Connection;

/**
 * WebSocket Channel Class
 * 
 * @author coltware@gmail.com
 *
 */
public interface WSChannel {
	
	public String getPath();
	
	public boolean accept(Connection conn,Map<String, String> map);
	
	/**
	 * Request Method
	 * 
	 * @param input
	 * @return output
	 */
	public Object request(Object input) throws Exception;
	
	public Class inputClass();
	
	public Class outputClass();
	
}
