package com.coltware.axion.main;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.coltware.axion.server.IAxionServerMXBean;

public class CLIStop {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		JMXServiceURL url = new JMXServiceURL(IAxionServerMXBean.SERVICE_ADDRESS);
		JMXConnector connector = JMXConnectorFactory.connect(url);
		MBeanServerConnection conn = connector.getMBeanServerConnection();
		ObjectName objname = new ObjectName(IAxionServerMXBean.OBJECT_NAME);
		
		conn.invoke(objname, "stop", null, null);
	}

}
