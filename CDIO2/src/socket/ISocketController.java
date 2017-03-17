package socket;

public interface ISocketController extends Runnable
{
	
	
	void registerObserver(ISocketObserver observer);
	void unRegisterObserver(ISocketObserver observer);
	
	void sendMessage(SocketOutMessage message);


}
