package axion.request;


public class DefaultPostRequest extends AbstractRequest implements PostRequest {

	@Override
	public Type getType() {
		return Request.Type.POST;
	}
}
