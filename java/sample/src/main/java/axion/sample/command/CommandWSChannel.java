package axion.sample.command;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.Connection;
import axion.channel.AbstractWSTemplateChannel;
import axion.channel.WSCommandChannel;

public class CommandWSChannel extends AbstractWSTemplateChannel implements WSCommandChannel{

	private static final Logger log = LoggerFactory.getLogger(CommandWSChannel.class);
	
	@Override
	public boolean accept(Connection conn, Map<String, String> params) {
		log.info("accept ...");
		if(!params.containsKey("uid")){
			log.info("uid parameter is NULL");
			return false;
		}
		String uid = params.get("uid");
		log.info("uid is [" + uid + "]");
		//  check uid 
		return true;
	}
}
