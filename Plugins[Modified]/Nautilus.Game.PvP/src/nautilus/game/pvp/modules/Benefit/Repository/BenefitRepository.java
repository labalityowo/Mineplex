package nautilus.game.pvp.modules.Benefit.Repository;

import java.util.List;

import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;

import mineplex.core.server.RemoteRepository;
import mineplex.core.server.remotecall.JsonWebCall;

public class BenefitRepository extends RemoteRepository
{
	public BenefitRepository(String webserverAddress)
	{
		super(webserverAddress);
	}
	
	public List<BenefitItemToken> GetBenefitItems(List<BenefitItemToken> itemTokens)
	{
		return new JsonWebCall(WebServerAddress + "Dominate/GetBenefitItems").Execute(new TypeToken<List<BenefitItemToken>>(){}.getType(), itemTokens);
	}
}
