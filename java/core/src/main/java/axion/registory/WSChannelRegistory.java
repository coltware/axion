package axion.registory;

import axion.channel.WSChannel;

public interface WSChannelRegistory {
	
	public void add(WSChannel channel);
	public void remove(WSChannel channel);
	
	public WSChannel get(String path);
	public boolean has(String path);
	
	public WSChannel find(String path);
	
	public int size();
}
