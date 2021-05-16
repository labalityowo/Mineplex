package mineplex.cache.player;

import java.util.UUID;

import mineplex.serverdata.Utility;
import mineplex.serverdata.data.Data;

public class PlayerInfo implements Data
{
	private int _id;
	private int _accountId;
	private UUID _uuid;
	private String _name;
	private boolean _online;
	private long _lastUniqueLogin;
	private long _loginTime;
	private int _sessionId;
	private int _version;
	
	public PlayerInfo(int id, UUID uuid, String name, int version)
	{
		_id = id;
		_uuid = uuid;
		_name = name;
		_version = version;
	}
	
	@Override
	public String getDataId()
	{
		return _uuid.toString();
	}
	
	public int getId()
	{
		return _id;
	}
	
	public UUID getUUID()
	{
		return _uuid;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean getOnline()
	{
		return _online;
	}
	
	public long getLastUniqueLogin()
	{
		return _lastUniqueLogin;
	}
	
	public long getLoginTime()
	{
		return _loginTime;
	}
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public int getVersion()
	{
		return _version;
	}

	public void setSessionId(int sessionId)
	{
		_sessionId = sessionId;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setVersion(int version)
	{
		_version = version;
	}
	
	public void updateLoginTime()
	{
		_loginTime = Utility.currentTimeMillis();
	}
}
