package com.coltware.axion.handler;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

import axion.Context;

import com.coltware.axion.AttrKey;
import com.coltware.axion.AxionClassLoader;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class AxionServerInitializer extends ChannelInitializer<SocketChannel> {

	private static final Logger log = LoggerFactory.getLogger(AxionServerInitializer.class);
	
	private final Context context;
	private final UriTemplate template;
	private final Properties props;
	private boolean autoReload = false;
	
	public AxionServerInitializer(Context context,Properties props){
		this.context = context;
		String path = this.context.getPath();
		if(path != null){
			template = new UriTemplate(path);
		}
		else{
			template = null;
		}
		this.props = props;
		if(this.props.containsKey("autoreload")){
			String flg = this.props.getProperty("autoreload");
			if(flg.equals("1") || flg.equalsIgnoreCase("true")){
				this.autoReload = true;
			}
		}
	}
	
	@Override
	public void initChannel(SocketChannel channel) throws Exception {
		if(log.isDebugEnabled()){
			log.debug("initChannel:");
		}
		//  コンテキストを設定
		channel.attr(AttrKey.CONTEXT).set(this.context);
		channel.attr(AttrKey.PATH).set(this.template);
		
		if(this.autoReload){
			AxionClassLoader loader = new AxionClassLoader(this.getClass().getClassLoader());
			channel.attr(AttrKey.CLASS_LOADER).set(loader);
		}
		
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		
		AxionWebSocketServerHandler handler = new AxionWebSocketServerHandler();
		
		pipeline.addLast("handler",handler);
		
		pipeline.addLast("axion.in",new AxionRequestHandler());
		pipeline.addLast("axion.out",new AxionResponseEncoder());
		pipeline.addLast("axion.system",new AxionSystemMessageEncoder());
		pipeline.addLast("axion.message",new AxionMessageEncoder());
	}
	
	

}
