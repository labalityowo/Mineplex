package mineplex.serverdata.commands;

public class ServerTransfer 
{

	// The name of the player who is being transferred.
	private String _playerName;
	public String getPlayerName() { return _playerName; }
	
	// The name of the destination server in this ServerTransfer.
	private String _serverName;
	public String getServerName() { return _serverName; }
	
	/**
	 * Class constructor
	 * @param playerName - the name of the player being transferred.
	 * @param serverName - the name of the server to transfer the player to (destination).
	 */
	public ServerTransfer(String playerName, String serverName)
	{
		_playerName = playerName;
		_serverName = serverName;
	}
}
