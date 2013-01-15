package axion.channel;

import java.util.List;

import axion.Connection;
import axion.data.EventMessage;

public interface WSPubSubChannel extends WSChannel {
	
	public enum AccessType {
		ALL("all"),
		AUTHORIZE("auth");
		
		private String name;
		private AccessType(String n){
			this.name = n;
		}
		
	}
	
	/**
	 * Access Type
	 * @return
	 */
	public AccessType getAccessType();
	public void setAccessType(AccessType type);
	
	public boolean subscribe(Connection conn);
	public boolean unsubscribe(Connection conn);
	
	public boolean has(Connection conn);
	
	public int size();
	
	/**
	 * Subscribed Connections
	 * @return
	 */
	public List<Connection> connections();
	
	public boolean hasEvent(String name);
	
	public boolean bind(String name,Connection conn);
	public boolean unbind(String name,Connection conn);
	
	public boolean fireEvent(EventMessage event);
}
