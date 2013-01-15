package axion.responder;

import java.util.Map;

public interface Responder<I,O> {
	public O response(I in,Map<String, String> pathParams) throws Exception;
}
