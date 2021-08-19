package mineplex.core.giveaway;

public class GiveawayCooldown
{
	private int _id;
	private String _name;
	private int _cooldown;

	public GiveawayCooldown(int id, String name, int cooldown)
	{
		_id = id;
		_name = name;
		_cooldown = cooldown;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public int getCooldown()
	{
		return _cooldown;
	}
}
