package mineplex.serverdata.commands;

import java.util.UUID;

public class AddPunishCommand extends ServerCommand
{
	private final String _target;
	private final String _category;
	private final String _sentence;
	private final String _reason;
	private final long _duration;
	private final String _admin;
	private final String _adminUUID;
	private final int  _severity;

	public AddPunishCommand(String finalPlayerName, int severity, String category, String sentence, String reason, long duration, String finalCallerName, String uuid)
	{
		this._target = finalPlayerName;
		this._severity = severity;
		this._category = category;
		this._sentence = sentence;
		this._reason = reason;
		this._duration = duration;
		this._admin = finalCallerName;
		this._adminUUID = uuid;
	}

	@Override
	public void run() 
	{
	}
}
