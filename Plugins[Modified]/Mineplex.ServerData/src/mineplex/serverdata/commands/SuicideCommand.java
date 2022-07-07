package mineplex.serverdata.commands;

import mineplex.serverdata.Region;

public class SuicideCommand extends ServerCommand
{
	private String _server;	
	public String getServerName() { return _server; }
	
	private Region _region;
	public Region getRegion() { return _region; }
	
	public SuicideCommand(String server, Region region)
	{
		_server = server;
		_region = region;
	}
	
	@Override
	public void run() 
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}
