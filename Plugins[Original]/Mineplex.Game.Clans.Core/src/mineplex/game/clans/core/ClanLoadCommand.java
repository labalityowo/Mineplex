package mineplex.game.clans.core;

import mineplex.serverdata.commands.ServerCommand;


public class ClanLoadCommand extends ServerCommand
{
	private String _clanName;
	public String getClanName() { return _clanName; }
	
	public ClanLoadCommand(String serverName, String clanName)
	{
		super(serverName);
		
		_clanName = clanName;
	}
	
	@Override
	public void run() 
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}
