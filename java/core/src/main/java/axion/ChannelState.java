package axion;

public interface ChannelState {
	/**
	 * Channelが接続された時
	 * @param session
	 */
	public void channelActive(Session session);
	/**
	 * Channelが切断された時
	 * @param session
	 */
	public void channelInactive(Session session);
}
