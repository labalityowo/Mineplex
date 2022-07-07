package mineplex.core.antihack.compedaccount;

import java.util.Objects;
import java.util.UUID;

public class PlayerInfo
{
	private final String _name;
	private final String _realName;
	private final UUID _uuid;
	private final int _accountId;
	private final String _ip;

	public PlayerInfo(String name, String realName, UUID uuid, int accountId, String ip)
	{
		_name = name;
		_realName = realName;
		_uuid = uuid;
		_accountId = accountId;
		_ip = ip;
	}

	public String getName()
	{
		return _name;
	}

	public String getRealName()
	{
		return _realName;
	}

	public UUID getUuid()
	{
		return _uuid;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public String getIp()
	{
		return _ip;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof PlayerInfo)) return false;
		PlayerInfo that = (PlayerInfo) o;
		return _accountId == that._accountId;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_accountId);
	}
}
