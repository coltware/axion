package axion.request;


public class DefaultPubSubRequest extends AbstractRequest implements
		PubSubRequest {
	
	protected Method method;
	
	@Override
	public Type getType() {
		return Request.Type.PUBSUB;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

}
