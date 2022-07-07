package mineplex.core.teamspeak.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class TeamspeakUnlinkRequest extends ServerCommand
{
	private final UUID _caller;
	private final int _id;

	public TeamspeakUnlinkRequest(UUID caller, int id)
	{
		this._caller = caller;
		this._id = id;
	}

	public UUID getCaller()
	{
		return _caller;
	}

	public int getId()
	{
		return _id;
	}
}
