package mineplex.core.chat;

public class MessageData
{

	private final String _message;
	private final long _timeSent;

	MessageData(String message)
	{
		_message = message;
		_timeSent = System.currentTimeMillis();
	}

	public String getMessage()
	{
		return _message;
	}

	public long getTimeSent()
	{
		return _timeSent;
	}
}