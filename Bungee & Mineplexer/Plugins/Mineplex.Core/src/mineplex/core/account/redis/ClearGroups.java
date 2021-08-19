package mineplex.core.account.redis;

import mineplex.serverdata.commands.ServerCommand;

public class ClearGroups extends ServerCommand
{
	private final int _accountId;
	
	public ClearGroups(int accountId)
	{
		_accountId = accountId;
	}
	
	public int getAccountId()
	{
		return _accountId;
	}
}