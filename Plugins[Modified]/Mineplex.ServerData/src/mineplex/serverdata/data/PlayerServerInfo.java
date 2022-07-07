package mineplex.serverdata.data;

public class PlayerServerInfo implements Data
{

	private String _playerName;
	public String getPlayerName() { return _playerName; }
	
	private String _lastServer;
	public String getLastServer() { return _lastServer; }
	public void setLastServer(String lastServer) { _lastServer = lastServer; }
	
	public PlayerServerInfo(String playerName, String lastServer)
	{
		_playerName = playerName;
		_lastServer = lastServer;
	}
	
	@Override
	public String getDataId() 
	{
		return _playerName;
	}

}
