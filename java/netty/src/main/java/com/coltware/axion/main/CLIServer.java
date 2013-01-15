package com.coltware.axion.main;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coltware.axion.server.AxionServer;
import com.coltware.axion.server.IAxionServerMXBean;

public class CLIServer {
	
	private static final int JMX_PORT = 1099;
	private static final Logger log = LoggerFactory.getLogger(CLIServer.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		LocateRegistry.createRegistry(JMX_PORT);
		MBeanServer mbserver = ManagementFactory.getPlatformMBeanServer();
		
		AxionServer server = new AxionServer();
		
		ObjectName objname = new ObjectName(IAxionServerMXBean.OBJECT_NAME);
		mbserver.registerMBean(server,objname);
		
		JMXServiceURL url = new JMXServiceURL(IAxionServerMXBean.SERVICE_ADDRESS);
		JMXConnectorServer jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(
				url, null, mbserver);
		jmxServer.start();
		
		server.start();
		
		jmxServer.stop();
		
	}
}
