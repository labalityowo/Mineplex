package mineplex.game.clans.core.war;

import java.sql.Timestamp;

public class ClanWarData
{
	private String _clanA;
	private String _clanB;
	private volatile int _clanAPoints;
	private Timestamp _timeFormed;
	private Timestamp _lastUpdated;
	private long _cooldown;

	public ClanWarData(String clanA, String clanB, int clanAPoints, Timestamp timeFormed, Timestamp lastUpdated, long cooldown)
	{
		_clanA = clanA;
		_clanB = clanB;
		_clanAPoints = clanAPoints;
		_timeFormed = timeFormed;
		_lastUpdated = lastUpdated;
		_cooldown = cooldown;
	}

	public String getClanA()
	{
		return _clanA;
	}

	public String getClanB()
	{
		return _clanB;
	}

	public int getClanAPoints()
	{
		return _clanAPoints;
	}

	public int getClanBPoints()
	{
		return -_clanAPoints;
	}

	public int getPoints(String clan)
	{
		if (_clanA.equals(clan))
			return getClanAPoints();
		else if (_clanB.equals(clan))
			return getClanBPoints();

		throw new RuntimeException("ClanWarData::getPoints Invalid Clan for War. ClanA: " + _clanA + ", ClanB: " + _clanB + ", Lookup Clan: " + clan);
	}

	public int getPoints()
	{
		return _clanAPoints;
	}

	public Timestamp getTimeFormed()
	{
		return _timeFormed;
	}

	public Timestamp getLastUpdated()
	{
		return _lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated)
	{
		_lastUpdated = lastUpdated;
	}

	public long getCooldown()
	{
		return _cooldown;
	}

	public void setCooldown(long cooldown)
	{
		_cooldown = System.currentTimeMillis() + cooldown;
	}

	public boolean isOnCooldown()
	{
		return _cooldown >= System.currentTimeMillis();
	}

	public void resetPoints()
	{
		update();

		_clanAPoints = 0;
	}

	public void increment(String clan)
	{
		update();

		if (_clanA.equals(clan))
			_clanAPoints++;
		else if (_clanB.equals(clan))
			_clanAPoints--;
		else throw new RuntimeException("ClanWarData::increment Invalid Clan for War. ClanA: " + _clanA + ", ClanB: " + _clanB + ", Lookup Clan: " + clan);
	}
	
	public void set(String clan, int points)
	{
		update();

		if (_clanA.equals(clan))
			_clanAPoints++;
		else if (_clanB.equals(clan))
			_clanAPoints--;
		else throw new RuntimeException("ClanWarData::increment Invalid Clan for War. ClanA: " + _clanA + ", ClanB: " + _clanB + ", Lookup Clan: " + clan);
	}

	private void update()
	{
		setLastUpdated(new Timestamp(System.currentTimeMillis()));
	}
}
