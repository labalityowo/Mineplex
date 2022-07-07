package mineplex.game.clans.clans.moderation.antialt;

import mineplex.serverdata.commands.ServerCommand;

public class IpBanNotification extends ServerCommand
{
	private final String _ip;
	
	public IpBanNotification(String ip)
	{
		_ip = ip;
	}
	
	public String getIp()
	{
		return _ip;
	}
}