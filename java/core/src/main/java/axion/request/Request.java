package axion.request;

import axion.Connection;
import axion.channel.WSChannel;

public interface Request {

	public enum Type{
		POST,
		PUBSUB,
		MESSAGE,
		EVENT,
	}
	
	public int getSequence();
	
	public Type getType();
	
	public String getPath();
	
	public Object getContent();
	
	public Connection getConnection();
	
	public WSChannel getChannel();
}
