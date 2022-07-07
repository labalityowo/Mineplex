package mineplex.core.party;

import java.util.UUID;

/**
 * Serializable invite data
 */
public class InviteData
{
	private final String _inviterName;
	private final UUID _inviterUUID;
	private final UUID _partyUUID;
	private final String _serverName;

	public InviteData(String inviterName, UUID inviterUUID, UUID partyUUID, String serverName)
	{
		_inviterName = inviterName;
		_inviterUUID = inviterUUID;
		_partyUUID = partyUUID;
		_serverName = serverName;
	}

	public String getInviterName()
	{
		return _inviterName;
	}

	public UUID getInviterUUID()
	{
		return _inviterUUID;
	}

	public UUID getPartyUUID()
	{
		return _partyUUID;
	}

	public String getServerName()
	{
		return _serverName;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InviteData that = (InviteData) o;

		return _partyUUID.equals(that._partyUUID);
	}

	@Override
	public int hashCode()
	{
		return _partyUUID.hashCode();
	}
}
