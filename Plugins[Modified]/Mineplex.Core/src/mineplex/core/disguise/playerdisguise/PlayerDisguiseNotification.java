package mineplex.core.disguise.playerdisguise;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

public class PlayerDisguiseNotification extends ServerCommand
{
	private String _realPlayerName;
	private UUID _playerUUID;
	private boolean _disguising;
	private String _disguisedName;
	private String _disguisedSkin;

	public PlayerDisguiseNotification(String realPlayerName, UUID playerUUID, String disguisedName, String disguisedSkin)
	{
		_realPlayerName = realPlayerName;
		_playerUUID = playerUUID;
		_disguising = true;
		_disguisedName = disguisedName;
		_disguisedSkin = disguisedSkin;
	}

	public PlayerDisguiseNotification(String realPlayerName, UUID playerUUID, String disguisedName)
	{
		_realPlayerName = realPlayerName;
		_playerUUID = playerUUID;
		_disguising = false;
		_disguisedName = disguisedName;
		_disguisedSkin = null;
	}

	public String getRealPlayerName()
	{
		return _realPlayerName;
	}

	public UUID getPlayerUUID()
	{
		return _playerUUID;
	}

	public boolean isDisguising()
	{
		return _disguising;
	}

	public String getDisguisedName()
	{
		return _disguisedName;
	}

	public String getDisguisedSkin()
	{
		return _disguisedSkin;
	}
}
