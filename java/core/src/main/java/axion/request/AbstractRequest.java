package axion.request;

import io.netty.handler.codec.http.HttpRequest;
import axion.Connection;
import axion.channel.WSChannel;

public abstract class AbstractRequest implements Request {

	protected int sequence= 0;
	protected String targetPath;
	
	protected Connection connection;
	protected WSChannel channel;
	protected Object content;
	
	
	/**
	 * GETで接続された時のHttpRequestオブジェクト
	 */
	protected HttpRequest httpRequest;
	
	@Override
	public int getSequence() {
		return this.sequence;
	}
	
	public void setSequence(int val){
		this.sequence = val;
	}

	@Override
	public String getPath() {
		return this.targetPath;
	}
	
	public void setTargetPath(String path){
		this.targetPath = path;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public WSChannel getChannel() {
		return channel;
	}

	public void setChannel(WSChannel channel) {
		this.channel = channel;
	}
}
