package mineplex.game.clans.clans;

import java.util.UUID;

public class ClansPlayer
{
	private String _playerName;
	private UUID _uuid;
	private ClanRole _role;
	private boolean _online;
	
	public ClansPlayer(String playerName, UUID uuid, ClanRole role)
	{
		_playerName = playerName;
		_uuid = uuid;
		_role = role;
		_online = false;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public UUID getUuid()
	{
		return _uuid;
	}

	public ClanRole getRole()
	{
		return _role;
	}

	public void setRole(ClanRole role)
	{
		_role = role;
	}

	public void setPlayerName(String playerName)
	{
		_playerName = playerName;
	}

	public boolean isOnline()
	{
		return _online;
	}

	public void setOnline(boolean online)
	{
		_online = online;
	}
}
