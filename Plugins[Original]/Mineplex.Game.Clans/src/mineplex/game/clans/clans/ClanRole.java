package mineplex.game.clans.clans;

public enum ClanRole
{
	NONE(0, "None"), RECRUIT(100, "Recruit"), MEMBER(500, "Member"), ADMIN(1000, "Admin"), LEADER(Integer.MAX_VALUE, "Leader");

	private int _powerValue;
	private String _friendlyName;

	ClanRole(int powerValue, String friendlyName)
	{
		_powerValue = powerValue;
		_friendlyName = friendlyName;
	}

	public boolean has(ClanRole role)
	{
		return getPowerValue() >= role.getPowerValue();
	}

	public String getFriendlyName()
	{
		return _friendlyName;
	}

	public int getPowerValue()
	{
		return _powerValue;
	}
}

