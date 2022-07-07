package mineplex.core.friend.redis;

import mineplex.serverdata.commands.ServerCommand;

public class FriendAddMessage extends ServerCommand
{

	private final String _accepter, _target;

	public FriendAddMessage(String accepter, String target)
	{
		_accepter = accepter;
		_target = target;
	}

	public String getAccepter()
	{
		return _accepter;
	}

	public String getTarget()
	{
		return _target;
	}
}
