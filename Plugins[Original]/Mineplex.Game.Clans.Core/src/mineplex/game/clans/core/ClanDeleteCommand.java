package mineplex.game.clans.core;

import mineplex.game.clans.core.repository.tokens.SimpleClanToken;
import mineplex.serverdata.commands.ServerCommand;


public class ClanDeleteCommand extends ServerCommand
{
	private String _clanName;
	public String getClanName() { return _clanName; }
	
	public ClanDeleteCommand(String serverName, String clanName)
	{
		super(serverName);
		
		_clanName = clanName;
	}
	
	public ClanDeleteCommand(SimpleClanToken clan)
	{
		this(clan.getHomeServer(), clan.getClanName());
	}
	
	@Override
	public void run() 
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}
