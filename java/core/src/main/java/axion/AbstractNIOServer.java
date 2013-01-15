package axion;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioEventLoopGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNIOServer implements IServer {

	private static final Logger log = LoggerFactory.getLogger(AbstractNIOServer.class);
	private ServerBootstrap boot;
	
	private int port = 0;
	
	@SuppressWarnings("rawtypes")
	private ChannelInitializer initializer;
	
	private Channel channel;
	
	public void setInitializer(@SuppressWarnings("rawtypes") ChannelInitializer initializer){
		this.initializer = initializer;
	}
	
	public Channel getChannel(){
		return this.channel;
	}
	
	@Override
	public void start() throws Exception{
		log.debug("start");
		boot = new ServerBootstrap();
		NioEventLoopGroup parentGroup = new NioEventLoopGroup();
		NioEventLoopGroup childGroup	= new NioEventLoopGroup();
		
		boot.group(parentGroup, childGroup);
		boot.localAddress(this.port);
		boot.childHandler(this.initializer);
		
		this.channel = this.boot.bind().sync().channel();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}
}
