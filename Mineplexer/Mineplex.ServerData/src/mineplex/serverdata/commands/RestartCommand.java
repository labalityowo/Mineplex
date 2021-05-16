package mineplex.serverdata.commands;

import mineplex.serverdata.Region;

public class RestartCommand extends ServerCommand
{

	private final String _server;
	private final Region _region;
	private final boolean _groupRestart;

	public RestartCommand(String server, Region region, boolean groupRestart)
	{
		_server = server;
		_region = region;
		_groupRestart = groupRestart;
	}

	@Override
	public void run()
	{
	}

	public String getServerName()
	{
		return _server;
	}

	public Region getRegion()
	{
		return _region;
	}

	public boolean isGroupRestart()
	{
		return _groupRestart;
	}
}
