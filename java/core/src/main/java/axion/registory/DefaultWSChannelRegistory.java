package axion.registory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.channel.WSChannel;
import axion.channel.WSTemplateChannel;

public class DefaultWSChannelRegistory implements WSChannelRegistory {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultWSChannelRegistory.class);

	private Map<String, WSChannel> channelMap;
	
	public DefaultWSChannelRegistory(){
		channelMap = new LinkedHashMap<String, WSChannel>();
	}
	
	@Override
	public void add(WSChannel channel) {
		String path = channel.getPath();
		channelMap.put(path, channel);
	}
	
	public void setChannel(WSChannel channel){
		this.add(channel);
	}
	
	public void setChannelList(List<WSChannel> list){
		for(WSChannel ch : list){
			this.add(ch);
		}
	}

	@Override
	public void remove(WSChannel channel) {
		String path = channel.getPath();
		if(channelMap.containsKey(path)){
			channelMap.remove(path);
		}
	}

	@Override
	public WSChannel get(String path) {
		if(channelMap.containsKey(path)){
			return channelMap.get(path);
		}
		return null;
	}

	@Override
	public boolean has(String path) {
		return channelMap.containsKey(path);
	}

	@Override
	public int size() {
		return channelMap.size();
	}
	
	public Set<String> pathList(){
		return channelMap.keySet();
	}

	@Override
	public WSChannel find(String path) {
		for(WSChannel ch : channelMap.values()){
			if(ch instanceof WSTemplateChannel){
				log.debug("template channel: " + path + "?" + ch.getPath());
				boolean matches = ((WSTemplateChannel)ch).matches(path);
				if(matches){
					return ch;
				}
			}
			else{
				if(ch.getPath().equals(path)){
					return ch;
				}
			}
		}
		return null;
	}
}
