package nautilus.game.minekart.repository;

import java.util.List;

import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;

import mineplex.core.server.remotecall.JsonWebCall;

public class KartRepository
{
	private String _webAddress;
	
	public KartRepository(String webserverAddress)
	{
		_webAddress = webserverAddress;
	}
	
	public List<KartItemToken> GetKartItems(List<KartItemToken> itemTokens)
	{
		return new JsonWebCall(_webAddress + "MineKart/GetKartItems").Execute(new TypeToken<List<KartItemToken>>(){}.getType(), itemTokens);
	}
}
