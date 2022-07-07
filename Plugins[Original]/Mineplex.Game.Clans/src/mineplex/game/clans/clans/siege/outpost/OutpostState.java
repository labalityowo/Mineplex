package mineplex.game.clans.clans.siege.outpost;

public enum OutpostState
{
	/**
	 * this much space between them so that there is some room for any potential new states.
	 */
	AWAITING(10),
	CONSTRUCTING(20),
	LIVE(30),
	DESTRUCTING(40),
	DEAD(50);
	
	private byte _id;
	
	OutpostState(int id)
	{
		_id = (byte) id;
	}

	public static OutpostState ById(byte id)
	{
		for (OutpostState state : values())
		{
			if (state._id == id)
			{
				return state;
			}
		}
		
		return null;
	}

	public byte getId()
	{
		return _id;
	}
}
