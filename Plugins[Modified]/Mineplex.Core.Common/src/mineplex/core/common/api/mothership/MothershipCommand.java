package mineplex.core.common.api.mothership;

import mineplex.serverdata.commands.ServerCommand;

public class MothershipCommand extends ServerCommand
{
	private Action action;

	public Action getAction()
	{
		return action;
	}

	public enum Action
	{
		CLEANUP, START
	}
}
