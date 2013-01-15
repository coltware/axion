package axion.channel;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.Connection;
import axion.ConnectionState;
import axion.data.EventMessage;

public class DefaultWSPubSubChannel extends AbstractWSChannel implements
		WSPubSubChannel,ConnectionState {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultWSPubSubChannel.class);
	
	private List<Connection> conns;
	private Map<String , List<Connection>> bindMap;
	
	
	private AccessType accessType;
	
	public DefaultWSPubSubChannel(){
		conns = Collections.synchronizedList(new LinkedList<Connection>());
		bindMap = new HashMap<String, List<Connection>>();
		this.accessType = WSPubSubChannel.AccessType.ALL;
	}

	@Override
	public boolean accept(Connection conn, Map<String, String> map) {
		return true;
	}


	@Override
	public boolean subscribe(Connection conn) {
		for(Connection c : conns){
			if(c == conn){
				return true;
			}
		}
		conns.add(conn);
		conn.addStateHandler(this);
		return true;
	}

	@Override
	public boolean unsubscribe(Connection conn) {
		for(Connection c : conns){
			if(c == conn){
				conns.remove(c);
				return true;
			}
		}
		return true;
	}
	
	

	@Override
	public boolean has(Connection conn) {
		for(Connection c : conns){
			if(c == conn){
				return true;
			}
		}
		return false;
	}

	@Override
	public int size() {
		return conns.size();
	}

	@Override
	public void connectionClosed(Connection connection) {
		Set<String> keys = this.bindMap.keySet();
		List<String> targets = new LinkedList<String>();
		for(String key : keys){
			if(this.unbind(key, connection)){
				targets.add(key);
			}
		}
		
		unsubscribe(connection);
		log.debug(this.getPath() + " - connection closed.." + size());
		
		for(String name: targets){
			if(this.bindMap.containsKey(name) && this.bindMap.get(name).size() == 0){
				log.debug("clear bind list : " + name);
				this.bindMap.remove(name);
			}
		}
	}

	@Override
	public AccessType getAccessType() {
		return this.accessType;
	}

	@Override
	public void setAccessType(AccessType type) {
		this.accessType = type;
	}

	@Override
	public List<Connection> connections() {
		return this.conns;
	}
	
	

	@Override
	public boolean hasEvent(String name) {
		if(bindMap.containsKey(name)){
			log.debug("Found event bind :" + bindMap.get(name).size());
			if(bindMap.get(name).size() > 0){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			log.debug("not found event bind");
			return false;
		}
	}

	@Override
	public boolean bind(String name, Connection conn) {
		if(this.has(conn)){
			List<Connection> list = null;
			if(this.bindMap.containsKey(name)){
				list = this.bindMap.get(name);
				for(Connection c : list){
					if(c == conn){
						return true;
					}
				}
				list.add(conn);
				return true;
			}
			else{
				list = Collections.synchronizedList(new LinkedList<Connection>());
				list.add(conn);
				bindMap.put(name, list);
				return true;
			}
		}
		else{
			return false;
		}
	}

	@Override
	public boolean unbind(String name, Connection conn) {
		if(this.has(conn)){
			boolean remove = false;
			List<Connection> list = null;
			if(this.bindMap.containsKey(name)){
				list = this.bindMap.get(name);
				for(Connection c : list){
					if(c == conn){
						remove = true;
						break;
					}
				}
			}
			if(remove){
				list.remove(conn);
				return true;
			}
			return false;
		}
		else{
			return false;
		}
	}

	@Override
	public boolean fireEvent(EventMessage event) {
		String name = event.getName();
		if(this.bindMap.containsKey(name)){
			List<Connection> list = this.bindMap.get(name);
			for(Connection c : list){
				c.write(event);
			}
		}
		return true;
	}
	
	
}
