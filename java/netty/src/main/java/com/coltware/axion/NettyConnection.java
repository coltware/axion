package com.coltware.axion;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.Connection;
import axion.ConnectionState;

public class NettyConnection implements Connection {
	
	private static final Logger log = LoggerFactory.getLogger(NettyConnection.class);
	
	private Channel channel;
	private List<ConnectionState> stateHandlers;
	private ClassLoader loader = null;
	
	
	public NettyConnection(Channel channel){
		this.channel = channel;
		this.stateHandlers = new ArrayList<ConnectionState>();
		loader = new AxionClassLoader(this.getClass().getClassLoader());
	}

	@Override
	public boolean isOpen() {
		return this.channel.isOpen();
	}

	@Override
	public void addStateHandler(ConnectionState handler) {
		this.stateHandlers.add(handler);
	}
	
	@Override
	public void removeStateHandler(ConnectionState handler) {
		this.stateHandlers.remove(handler);
	}

	public void close(){
		for(ConnectionState handler: stateHandlers){
			handler.connectionClosed(this);
		}
		this.stateHandlers.clear();
	}

	@Override
	public void write(Object msg) {
		if(this.channel.isOpen() && this.channel.isActive()){
			this.channel.write(msg);
		}
		else{
			this.close();
		}
	}
}
