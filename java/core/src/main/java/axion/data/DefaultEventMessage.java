package axion.data;

public class DefaultEventMessage extends DefaultMessage implements EventMessage {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
