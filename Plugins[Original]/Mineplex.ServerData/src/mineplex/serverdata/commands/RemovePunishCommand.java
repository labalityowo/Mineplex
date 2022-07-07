package mineplex.serverdata.commands;

import java.util.UUID;

import com.google.gson.JsonObject;

public class RemovePunishCommand extends ServerCommand
{
	private final JsonObject _punishment;
	private final String _target;
	private final String _admin;
	private final String _adminUUID;
	private final String _reason;

	public RemovePunishCommand(JsonObject punishment, String target, String admin, UUID adminUUID, String reason)
	{
		_punishment = punishment;
		_target = target;
		_admin = admin;
		_adminUUID = adminUUID.toString();
		_reason = reason;
	}

	@Override
	public void run() 
	{
	}
}
