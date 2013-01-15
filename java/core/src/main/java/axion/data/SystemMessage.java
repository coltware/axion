package axion.data;

public interface SystemMessage {
	
	public static final String CONNECTED = "connected";
	
	public String getType();
	public String getValue();
}
