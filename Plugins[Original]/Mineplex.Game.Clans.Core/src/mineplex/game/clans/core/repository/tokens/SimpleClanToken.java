package mineplex.game.clans.core.repository.tokens;

public class SimpleClanToken 
{

	private String _clanName = "";
	public String getClanName() { return _clanName; }
	
	private String _clanRole;
	public String getClanRole() { return _clanRole; }
	
	private String _homeServer;
	public String getHomeServer() { return _homeServer; }
	
	private int _clanId;
	public int getClanId() { return _clanId; }
	
	public SimpleClanToken(String clanName, String clanRole, String homeServer, int clanId)
	{
		_clanName = clanName;
		_clanRole = clanRole;
		_homeServer = homeServer;
		_clanId = clanId;
	}

	public SimpleClanToken() { }
}
