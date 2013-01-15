package axion.responder.string;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.responder.Responder;

public class StringResponder implements Responder<String, String> {
	
	private static final Logger log = LoggerFactory.getLogger(StringResponder.class);

	@Override
	public String response(String in,Map<String, String> params) {
		log.debug("in[" + in + "]");
		return in;
	}
}
