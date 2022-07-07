package mineplex.game.clans.clans.war;

public class WarSiege
{
	private String _besiegedClan;
	private String _besiegingClan;
	private long _startTime;
	private long _endTime;

	public WarSiege(String besiegedClan, String besiegingClan)
	{
		_besiegedClan = besiegedClan;
		_besiegingClan = besiegingClan;
		_startTime = System.currentTimeMillis();
		_endTime = _startTime + WarManager.INVADE_LENGTH;
	}

	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}

	public String getBesiegedClan()
	{
		return _besiegedClan;
	}

	public String getBesiegingClan()
	{
		return _besiegingClan;
	}

	public long getStartTime()
	{
		return _startTime;
	}

	public long getEndTime()
	{
		return _endTime;
	}
}