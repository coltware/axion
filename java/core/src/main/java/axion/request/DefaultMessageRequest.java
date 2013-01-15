package axion.request;


public class DefaultMessageRequest extends AbstractRequest implements
		MessageRequest {

	@Override
	public Type getType() {
		return Request.Type.MESSAGE;
	}

}
