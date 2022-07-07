package mineplex.core.giveaway;

public class GiveawayReward
{
	private String _name;
	private String _uuid;

	public GiveawayReward(String name, String uuid)
	{
		_name = name;
		_uuid = uuid;
	}

	public String getName()
	{
		return _name;
	}

	public String getUuid()
	{
		return _uuid;
	}
}
