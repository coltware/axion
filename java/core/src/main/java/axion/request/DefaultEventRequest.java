package axion.request;

public class DefaultEventRequest extends AbstractRequest implements EventRequest{

	private String name;
	private String method;
	
	@Override
	public Type getType() {
		return Type.EVENT;
	}
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
