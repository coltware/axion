package axion.channel;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

import axion.responder.Responder;

public abstract class AbstractWSTemplateChannel extends AbstractWSChannel implements
		WSTemplateChannel, WSChannel {

	private static final Logger log = LoggerFactory.getLogger(AbstractWSTemplateChannel.class);
	
	private UriTemplate template;
	
	
	@Override
	public void setPath(String path) {
		super.setPath(path);
		this.template = new UriTemplate(path);
	}

	@Override
	public boolean matches(String path) {
		if(template == null){
			return false;
		}
		if(log.isDebugEnabled()){
			log.debug("template:" + template.getVariableNames());
		}
		return this.template.matches(path);
	}

	@Override
	public Map<String, String> match(String path) {
		if(this.template == null){
			return null;
		}
		return this.template.match(path);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object request(Object in, Map<String, String> args) throws Exception {
		log.debug("accept: " + in.getClass());
		if(this.template == null){
			return null;
		}
		if(this.responderClz != null){
			
			this.createResponder();
			
			@SuppressWarnings("rawtypes")
			Responder responder = this.createResponder();
			if(responder != null){
				return responder.response(in,args);
			}
			else{
				return null;
			}
		}
		else{
			log.info("responder is NULL");
			return null;
		}
	}
	
	
}
