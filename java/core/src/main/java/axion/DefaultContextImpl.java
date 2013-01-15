package axion;

import java.util.List;

import axion.channel.DefaultWSCommandChannel;
import axion.channel.WSChannel;
import axion.channel.WSCommandChannel;
import axion.registory.DefaultWSChannelRegistory;
import axion.registory.WSChannelRegistory;


public class DefaultContextImpl implements Context {

	private final DefaultWSChannelRegistory channelRegistory;
	private final WSCommandChannel systemCommandChannel;
	
	private String path;
	
	public DefaultContextImpl(){
		channelRegistory = new DefaultWSChannelRegistory();
		systemCommandChannel = new DefaultWSCommandChannel();
		channelRegistory.add(systemCommandChannel);
	}

	@Override
	public WSChannelRegistory getWSChannelRegistroy() {
		return channelRegistory;
	}

	@Override
	public WSCommandChannel getSystemCommandChannel() {
		return systemCommandChannel;
	}
	
	public void setPath(String value){
		this.path = value;
	}
	
	public String getPath(){
		return path;
	}
	
	/**
	 *  Channelを登録する
	 * @param channel
	 */
	public void setChannel(WSChannel channel){
		channelRegistory.add(channel);
	}
	
	public void setChannels(List<WSChannel> list){
		channelRegistory.setChannelList(list);
	}
}
