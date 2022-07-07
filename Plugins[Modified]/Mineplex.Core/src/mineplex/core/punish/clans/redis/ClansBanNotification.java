package mineplex.core.punish.clans.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class ClansBanNotification extends ServerCommand
{
	private final UUID _target;
	private final String _banTimeFormatted;
	
	public ClansBanNotification(UUID target, String banTimeFormatted)
	{
		_target = target;
		_banTimeFormatted = banTimeFormatted;
	}
	
	public UUID getTarget()
	{
		return _target;
	}
	
	public String getBanTimeFormatted()
	{
		return _banTimeFormatted;
	}
}