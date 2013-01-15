package axion;

public interface Response {
	
	public int getSequence();
	public int getCode();
	public String getMessage();
	public Object getContent();
}
