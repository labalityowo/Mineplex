package mineplex.core.chatsnap;

/**
 * Holds all types of messages a player can receive from another player
 */
public enum MessageType
{
	CHAT(0),
	PM(1),
	PARTY(2);

	private final int _id;

	MessageType(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}
}
