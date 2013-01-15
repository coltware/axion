package axion.channel;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.Connection;

public class DefaultWSCommandChannel extends AbstractWSChannel implements WSCommandChannel {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultWSCommandChannel.class);
	
	public DefaultWSCommandChannel(){
		String path = UUID.randomUUID().toString();
		this.setPath("/" + path);
	}

	@Override
	public boolean accept(Connection conn, Map<String, String> map) {
		return true;
	}
	
	
}
