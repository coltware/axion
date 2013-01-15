package axion.sample.command;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.ns.Path;
import axion.responder.Responder;

public class CommandResponder implements Responder<Object, Object>{
	
	private static final Logger log = LoggerFactory.getLogger(CommandResponder.class);

	@Path("/command/{method}")
	public Object response(Object in, Map<String, String> pathParams) throws Exception {
		String method = pathParams.get("method");
		Object id = 0;
		if(in instanceof Map){
			Map<String, Object> args = (Map)in; 
			log.debug("(" + method + ") in[" + args.get("id") + "]");
			id = args.get("id");
		}
		else{
			return null;
		}
		
		if(method.equals("error")){
			throw new UnsupportedOperationException("can't call [" + method  + "] method");
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String now = sdf.format(Calendar.getInstance().getTime());
		return id + " --> invoked(" + now  + "): method is [" + method + "]";
	}
}
