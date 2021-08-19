package mineplex.core.teamspeak.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class TeamspeakLinkRequest extends ServerCommand
{
	private final UUID _caller;
	private final String _token;

	public TeamspeakLinkRequest(UUID caller, String token)
	{
		this._caller = caller;
		this._token = token;
	}

	public UUID getCaller()
	{
		return _caller;
	}

	public String getToken()
	{
		return _token;
	}
}
