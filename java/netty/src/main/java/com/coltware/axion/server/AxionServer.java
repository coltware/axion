package com.coltware.axion.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import axion.Context;
import axion.registory.DefaultWSChannelRegistory;
import axion.registory.WSChannelRegistory;

import com.coltware.axion.handler.AxionServerInitializer;

public class AxionServer implements IAxionServerMXBean {
	
	private static final Logger log = LoggerFactory.getLogger(AxionServer.class);
	private static final ChannelGroup allChannels = new DefaultChannelGroup("axion-servers");
	
	private Boolean started = false;
	private int port = 8081;
	

	private ServerBootstrap bootstrap;
	private Channel channel;

	private Context context;
	private ApplicationContext springContext;
	private Properties props;
	
	public AxionServer(){
		//context = new DefaultContextImpl();
		this.init();
	}
	
	public void init(){
		String springXml = "application.xml";
		springContext = new ClassPathXmlApplicationContext(springXml);
		context = springContext.getBean("axion.context", Context.class);
		log.debug("content:" + context);
		
		Properties props = springContext.getBean("axion.properties", Properties.class);
		if(props.containsKey("port")){
			this.port = Integer.valueOf((String)props.get("port"));
		}
		this.props = props;
		
		if(log.isErrorEnabled()){
			WSChannelRegistory channelRegistory = context.getWSChannelRegistroy();
			log.debug("channel size:[" + channelRegistory.size() + "]");
			if(channelRegistory instanceof DefaultWSChannelRegistory){
				Set<String> names = ((DefaultWSChannelRegistory) channelRegistory).pathList();
				for(String name : names){
					log.debug("register path:" + name);
				}
			}
		}
	}
	
	@Override
	public void start() throws Exception{
		log.debug("axion server start");
		if(!started){
			synchronized (started) {
				
				started = true;
				bootstrap = new ServerBootstrap();
				NioEventLoopGroup parentGroup	= new NioEventLoopGroup();
				NioEventLoopGroup childGroup	= new NioEventLoopGroup();
				
				try{
						
					bootstrap.group(parentGroup, childGroup);
					bootstrap.channel(NioServerSocketChannel.class);
					bootstrap.localAddress(this.port);
					bootstrap.childHandler(new AxionServerInitializer(this.context,this.props));
					
					channel = bootstrap.bind().sync().channel();
					log.debug("channel is [" + channel + "] opened");
					allChannels.add(channel);
					channel.closeFuture().sync();
					
					//log.debug("sync end axion server started..." + channel.localAddress());
				}
				finally{
					bootstrap.shutdown();
					log.debug("shutdown .....ok");
					started = false;
				}
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					bootstrap.shutdown();
				}
				catch(Exception ex){
					log.error(ex.getMessage(),ex);
				}
				finally{
					log.debug("shutdown... ok");
				}
			}
		}));
	}

	@Override
	public void stop() {
		log.info("axion server stop....");
		if(started){
			try{
				allChannels.close().awaitUninterruptibly();
				
			}
			catch(Exception ex){
				log.error(ex.getMessage(),ex);
			}
		}
	}

}
