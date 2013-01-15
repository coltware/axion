package com.coltware.axion.server;

public interface IAxionServerMXBean {
	
	public static final String SERVICE_ADDRESS = "service:jmx:rmi:///jndi/rmi://localhost/axion";
	public static final String OBJECT_NAME = "com.coltware.axion:Type=AxionServer";
	
	
	public void start() throws Exception;
	public void stop();
}
