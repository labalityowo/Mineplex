package mineplex.core.teamspeak;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TeamspeakClientInfo
{
	private Map<Integer, Date> _linkedAccounts = new HashMap<>();

	public TeamspeakClientInfo(Map<Integer, Date> linkedAccounts)
	{
		_linkedAccounts = new HashMap<>(linkedAccounts);
	}

	public Map<Integer, Date> getLinkedAccounts()
	{
		return Collections.unmodifiableMap(_linkedAccounts);
	}

	public void unlink(int account)
	{
		_linkedAccounts.remove(account);
	}

	public void link(int id, Date now)
	{
		_linkedAccounts.put(id, now);
	}
}
