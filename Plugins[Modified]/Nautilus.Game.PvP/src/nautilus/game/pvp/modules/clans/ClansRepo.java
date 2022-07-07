package nautilus.game.pvp.modules.clans;

import java.util.List;

import nautilus.game.pvp.modules.clans.ClansClan.Role;
import nautilus.game.pvp.modules.clans.Tokens.AllianceToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanMemberToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanTerritoryToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanToken;
import nautilus.game.pvp.modules.clans.Tokens.WarToken;

import me.chiss.Core.Plugin.IPlugin;

public class ClansRepo 
{
	private Clans Clans;
	public ClanRepository Repository;
	
	public ClansRepo(Clans clans)
	{
		Clans = clans;
		Repository = new ClanRepository(((IPlugin)Clans.Plugin()).GetWebServerAddress());
	}

	public void loadClans()
	{
		List<ClanToken> clanTokens = Repository.GetClans(Clans.GetServerName());

		//Create Clans
		for (ClanToken token : clanTokens)
		{
			//Create Clan
			ClansClan clan = new ClansClan(Clans, token);
			Clans.GetClanMap().put(clan.GetName(), clan);

			//Add Members
			for (ClanMemberToken client : token.Members)
			{
				clan.GetMembers().put(client.Name, Role.valueOf(client.ClanRole.Name));
				Clans.GetClanMemberMap().put(client.Name, clan);
			}

			//Add Territory
			for (ClanTerritoryToken territory : token.Territories)
			{
				clan.GetClaimSet().add(territory.Chunk);

				//Register Claim
				Clans.GetClaimMap().put(territory.Chunk, new ClansTerritory(Clans, clan.GetName(), territory.Chunk, territory.Safe));	
			}	
		}

		//Add Relationships
		for (ClanToken token : clanTokens)
		{
			ClansClan clan = Clans.getClan(token.Name);
			if (clan == null)
			{
				System.out.println("Clans Load Error: Could not find Clan " + token.Name);
				continue;
			}

			for (AllianceToken alliance : token.Alliances)
			{
				ClansClan other = Clans.getClan(alliance.ClanName);
				if (other == null)
				{
					System.out.println("Clans Load Error: Could not find Alliance Clan " + token.Name);
					continue;
				}

				clan.GetAllyMap().put(other.GetName(), alliance.Trusted);
			}

			for (WarToken warToken : token.Wars)
			{
				ClansClan other = Clans.getClan(warToken.ClanName);
				if (other == null)
				{
					System.out.println("Clans Load Error: Could not find War Clan " + token.Name);
					continue;
				}

				if (!warToken.Ended)
				{
					ClansWar war = new ClansWar(Clans, clan, other, warToken.Dominance, warToken.Created); 
					clan.GetEnemyOut().put(other.GetName(), war);
					other.GetEnemyIn().put(clan.GetName(), war);
				}
				else
				{
					//Clan
					if (clan.GetEnemyRecharge().containsKey(other.GetName()))
					{
						if (clan.GetEnemyRecharge().get(other.GetName()) < warToken.Cooldown)
							clan.GetEnemyRecharge().put(other.GetName(), warToken.Cooldown);
					}
					else
					{
						clan.GetEnemyRecharge().put(other.GetName(), warToken.Cooldown);
					}
					//Other
					if (other.GetEnemyRecharge().containsKey(clan.GetName()))
					{
						if (other.GetEnemyRecharge().get(clan.GetName()) < warToken.Cooldown)
							other.GetEnemyRecharge().put(clan.GetName(), warToken.Cooldown);
					}
					else
					{
						other.GetEnemyRecharge().put(clan.GetName(), warToken.Cooldown);
					}
				}
			}
		}
	}

	public void AddClan(ClanToken clanToken)
	{
		Repository.AddClan(clanToken);
	}

	public void EditClan(ClanToken clanToken)
	{
		Repository.EditClan(clanToken);
	}

	public void DeleteClan(ClanToken clanToken)
	{
		Repository.DeleteClan(clanToken);
	}
}
