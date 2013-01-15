package axion;

/**
 * WebSocket Connection
 * 
 * @author coltware@gmail.com
 *
 */
public interface Connection {
	
	public boolean isOpen();
	
	public void addStateHandler(ConnectionState handler);
	public void removeStateHandler(ConnectionState handler);
	
	public void write(Object msg);
}