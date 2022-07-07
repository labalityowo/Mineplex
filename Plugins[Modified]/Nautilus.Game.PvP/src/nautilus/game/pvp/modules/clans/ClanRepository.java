package nautilus.game.pvp.modules.clans;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.UtilWorld;
import mineplex.core.server.remotecall.AsyncJsonWebCall;
import mineplex.core.server.remotecall.JsonWebCall;
import nautilus.game.pvp.modules.clans.Tokens.ClanGeneratorToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanToken;

import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;

public class ClanRepository
{
	private String _webServerAddress;
	
	public ClanRepository(String webServerAddress)
	{
		_webServerAddress = webServerAddress;
	}
	
	public List<ClanToken> GetClans(String serverName) 
	{
		return new JsonWebCall(_webServerAddress + "Clan/GetClans").Execute(new TypeToken<List<ClanToken>>(){}.getType(), serverName);
	}

	public void AddClan(ClanToken clan) 
	{
		new AsyncJsonWebCall(_webServerAddress + "Clan/AddClan").Execute(clan);
	}

	public void EditClan(ClanToken clan) 
	{
		new AsyncJsonWebCall(_webServerAddress + "Clan/EditClan").Execute(clan);
	}

	public void DeleteClan(ClanToken clan) 
	{
		new AsyncJsonWebCall(_webServerAddress + "Clan/DeleteClan").Execute(clan);
	}

	public void UpdateClanTNTGenerators(List<ClansClan> genUpdateList)
	{
		List<ClanGeneratorToken> tokenList = new ArrayList<ClanGeneratorToken>(genUpdateList.size());

		for (ClansClan clan : genUpdateList)
		{
			ClanGeneratorToken token = new ClanGeneratorToken();
			token.Name = clan.GetName();
			token.Location = UtilWorld.locToStr(clan.GetGeneratorBlock());
			token.Stock = clan.GetGeneratorStock();
			token.Time = clan.GetGeneratorTime();
		}

		new AsyncJsonWebCall(_webServerAddress + "Clan/UpdateClanTNTGenerators").Execute(tokenList);
	}

	public void UpdateClanTNTGenerator(String name, Location generatorBlock, int generatorStock, long generatorTime)
	{
		ClanGeneratorToken token = new ClanGeneratorToken();
		token.Name = name;
		token.Location = UtilWorld.locToStr(generatorBlock);
		token.Stock = generatorStock;
		token.Time = generatorTime;

		new AsyncJsonWebCall(_webServerAddress + "Clan/UpdateClanTNTGenerator").Execute(token);
	}
}
