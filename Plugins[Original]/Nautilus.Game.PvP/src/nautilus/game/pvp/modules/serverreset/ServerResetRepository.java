package nautilus.game.pvp.modules.serverreset;

import mineplex.core.server.RemoteRepository;
import mineplex.core.server.remotecall.JsonWebCall;

public class ServerResetRepository extends RemoteRepository
{
	public ServerResetRepository(String webserverAddress)
	{
		super(webserverAddress);
	}
	
	public void ResetClanData()
	{
		new JsonWebCall(WebServerAddress + "Clan/ResetClanData").Execute();
	}
}
