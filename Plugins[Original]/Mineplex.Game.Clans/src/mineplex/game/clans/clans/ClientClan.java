package mineplex.game.clans.clans;

public class ClientClan
{
	private boolean _clanChat;
	private boolean _allyChat;
	
	private boolean _mapOn;
	private long _joinDelay;
	
	private String _territory = "";
	private boolean _autoClaim;
	private String _owner = "";
	private boolean _safe;
	private String _mimic = "";
	
	public boolean isAllyChat()
	{
		return _allyChat;
	}

	public void setAllyChat(boolean allyChat)
	{
		_allyChat = allyChat;
	}

	public boolean isClanChat()
	{
		return _clanChat;
	}

	public void setClanChat(boolean clanChat)
	{
		_clanChat = clanChat;
	}

	public boolean isMapOn()
	{
		return _mapOn;
	}

	public void setMapOn(boolean mapOn)
	{
		_mapOn = mapOn;
	}

	public boolean canJoin()
	{
		if (System.currentTimeMillis() > _joinDelay) 
			return true;
		
		return false;
	}

	public long getDelay()
	{
		return _joinDelay;
	}

	public String getTerritory()
	{
		return _territory;
	}

	public void setTerritory(String territory)
	{
		_territory = territory;
	}

	public boolean isAutoClaim()
	{
		return _autoClaim;
	}

	public String getOwner()
	{
		return _owner;
	}

	public void setOwner(String owner)
	{
		_owner = owner;
	}

	public boolean isSafe()
	{
		return _safe;
	}

	public void setSafe(boolean safe)
	{
		_safe = safe;
	}

	public void setAutoClaim(boolean autoclaim)
	{
		_autoClaim = autoclaim;
	}

	public void setMimic(String mimic)
	{
		_mimic = mimic;
	}
	
	public String getMimic()
	{
		return _mimic;
	}
}
