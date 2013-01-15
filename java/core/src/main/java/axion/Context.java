package axion;

import axion.channel.WSCommandChannel;
import axion.registory.WSChannelRegistory;

public interface Context {
	
	public WSChannelRegistory getWSChannelRegistroy();
	
	public WSCommandChannel getSystemCommandChannel();
	
	/**
	 * ws://host/<b>path</b>
	 * 
	 * @return
	 */
	public String getPath();
}
