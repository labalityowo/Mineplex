package mineplex.core.account.redis;

import mineplex.serverdata.commands.ServerCommand;

public class AddPermissionGroup extends ServerCommand
{
	private final int _accountId;
	private final String _groupIdentifier;
	
	public AddPermissionGroup(int accountId, String groupIdentifier)
	{
		_accountId = accountId;
		_groupIdentifier = groupIdentifier;
	}
	
	public int getAccountId()
	{
		return _accountId;
	}
	
	public String getGroupIdentifier()
	{
		return _groupIdentifier;
	}
}