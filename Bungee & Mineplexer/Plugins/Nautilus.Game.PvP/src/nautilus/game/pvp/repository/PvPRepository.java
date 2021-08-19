package nautilus.game.pvp.repository;

import java.util.List;

import mineplex.core.server.remotecall.AsyncJsonWebCall;
import mineplex.core.server.remotecall.JsonWebCall;
import nautilus.game.pvp.modules.Fishing.Fish;
import nautilus.game.pvp.modules.Fishing.FishData;

import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;

public class PvPRepository extends RemoteRepository
{
	public PvPRepository(String webServerAddress) 
	{
		super(webServerAddress);
	}

	public List<FishToken> GetFishingAllTimeHigh()
	{
		return new JsonWebCall(WebServerAddress + "Fishing/GetFishingAllTimeHigh").Execute(new TypeToken<List<FishToken>>(){}.getType(), (Object)null);
	}
	
	public List<FishToken> GetFishingDayHigh()
	{
		return new JsonWebCall(WebServerAddress + "Fishing/GetFishingDayHigh").Execute(new TypeToken<List<FishToken>>(){}.getType(), (Object)null);
	}

	public void ClearDailyFishingScores()
	{
		new AsyncJsonWebCall(WebServerAddress + "Fishing/ClearDailyFishingScores").Execute();
	}

	public void SaveFishingAllTimeHigh(Fish fish, FishData fishData)
	{
		FishToken token = new FishToken();
		token.Name = fish.name();
		token.Size = fishData.GetPounds();
		token.Catcher = fishData.GetCatcher();
		
		new AsyncJsonWebCall(WebServerAddress + "Fishing/SaveFishingAllTimeHigh").Execute(token);
	}

	public void SaveFishingDayHigh(Fish fish, FishData fishData)
	{
		FishToken token = new FishToken();
		token.Name = fish.name();
		token.Size = fishData.GetPounds();
		token.Catcher = fishData.GetCatcher();
		
		new AsyncJsonWebCall(WebServerAddress + "Fishing/SaveFishingDayHigh").Execute(token);
	}

	public void SaveFishingScores(Fish fish, FishData fishData)
	{
		FishToken token = new FishToken();
		token.Name = fish.name();
		token.Size = fishData.GetPounds();
		token.Catcher = fishData.GetCatcher();
		
		new AsyncJsonWebCall(WebServerAddress + "Fishing/SaveFishingScore").Execute(token);
	}
}
