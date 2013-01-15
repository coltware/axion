package axion.request;

public interface PubSubRequest extends Request {
	
	public enum Method{
		SUB("sub"),
		UNSUB("unsub");
		
		private String name;
		private Method(String name){
			this.name = name;
		}
		
		public static Method fromString(final String name){
			for(Method m : values()){
				if(m.name.equalsIgnoreCase(name)){
					return m;
				}
			}
			return null;
		}
	}
	
	public Method getMethod();
}
