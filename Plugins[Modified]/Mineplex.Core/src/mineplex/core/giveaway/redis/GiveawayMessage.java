package mineplex.core.giveaway.redis;

import mineplex.serverdata.commands.ServerCommand;

public class GiveawayMessage extends ServerCommand
{
	private String _giveawayName;
	private String _playerName;
	private String _giveawayHeader;
	private String _giveawayMessage;

	public GiveawayMessage(String giveawayName, String playerName, String giveawayMessage, String giveawayHeader)
	{
		_giveawayName = giveawayName;
		_playerName = playerName;
		_giveawayMessage = giveawayMessage;
		_giveawayHeader = giveawayHeader;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public String getGiveawayMessage()
	{
		return _giveawayMessage;
	}

	public String getGiveawayHeader()
	{
		return _giveawayHeader;
	}

	public String getGiveawayName()
	{
		return _giveawayName;
	}
}
