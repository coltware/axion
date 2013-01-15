package axion.data;


public class DefaultMessage implements Message {
	
	private String path;
	private Object body;

	@Override
	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public Object getBody() {
		// TODO Auto-generated method stub
		return this.body;
	}

}
