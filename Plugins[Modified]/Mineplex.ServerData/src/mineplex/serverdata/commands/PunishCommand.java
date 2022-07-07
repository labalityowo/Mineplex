package mineplex.serverdata.commands;


public class PunishCommand extends ServerCommand
{
	private String _playerName;
	private boolean _ban;
	private boolean _mute;
	private String _message;
	
	public String getPlayerName() { return _playerName; }
	public boolean getBan() { return _ban; }
	public boolean getMute() { return _mute; }
	public String getMessage() { return _message; }
	
	public PunishCommand(String playerName, boolean ban, boolean mute, String message)
	{
		_playerName = playerName;
		_ban = ban;
		_mute = mute;
		_message = message;
	}
	
	@Override
	public void run() 
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}
