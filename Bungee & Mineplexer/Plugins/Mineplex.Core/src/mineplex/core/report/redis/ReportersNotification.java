package mineplex.core.report.redis;

import java.util.Set;
import java.util.UUID;

import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.serverdata.commands.ServerCommand;

/**
 * When this packet is received by a server, it will check to see if any of the reporters are online.
 * If so, it will send the supplied notification to them.
 */
public class ReportersNotification extends ServerCommand
{
	private Set<UUID> _reporters;
	private String _message; // in json format

	public ReportersNotification(Set<UUID> ids, JsonMessage jsonMessage)
	{
		_reporters = ids;
		_message = jsonMessage.toString();
	}

	public Set<UUID> getReporterUUIDs()
	{
		return _reporters;
	}

	public String getJson()
	{
		return _message;
	}
}
