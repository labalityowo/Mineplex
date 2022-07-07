package nautilus.game.pvp.modules.clans;

import java.util.ArrayList;

import nautilus.game.pvp.modules.clans.ClansClan.Role;
import nautilus.game.pvp.modules.clans.Tokens.ClanMemberToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanRole;
import nautilus.game.pvp.modules.clans.Tokens.ClanToken;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilWorld;

public class ClansTask 
{
	private Clans Clans;
	public ClansTask(Clans clans)
	{
		Clans = clans;
	}

	public void delete(ClansClan clan)
	{
		//Territory Unclaim
		for (String cur : clan.GetClaimSet())
			Clans.GetClaimMap().remove(cur);

		Clans.GetClanMap().remove(clan.GetName());

		for (String cur : clan.GetMembers().keySet())
		{
			CoreClient client = Clans.Clients().GetNull(cur);

			Clans.GetClanMemberMap().remove(cur);

			if (client != null)
				client.Clan().Update("", "", clan.getTimer());
		}

		//Clean from Others
		for (ClansClan cur : Clans.GetClanMap().values())
		{
			cur.GetAllyMap().remove(clan.GetName());
			cur.GetEnemyIn().remove(clan.GetName()); 
			cur.GetEnemyOut().remove(clan.GetName()); 
			cur.GetEnemyRecharge().remove(clan.GetName()); 
			cur.GetRequestMap().remove(clan.GetName());
		}

		//Save
		Clans.CRepo().DeleteClan(clan.GetToken());

		//Update Relations
		for (String cur : clan.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Log
		Clans.log("Deleted [" + clan.GetName() + "].");
	}

	public ClansClan create(String creator, String name, boolean admin) 
	{
		ClanToken token = new ClanToken();
		token.Name = name;
		token.Description = "No Description";
		token.Home = "";
		token.Admin = admin;
		token.DateCreated = System.currentTimeMillis();
		token.LastTimeOnline = System.currentTimeMillis();
		token.Power = 0;

		token.Members = new ArrayList<ClanMemberToken>();
		ClanMemberToken memberToken = new ClanMemberToken();
		memberToken.ClanRole = new ClanRole();
		memberToken.ClanRole.Name = Role.ADMIN.toString();
		memberToken.Name = creator;

		//Create Clan
		ClansClan clan = new ClansClan(Clans, token);
		Clans.GetClanMap().put(name, clan);

		//Save
		Clans.CRepo().AddClan(token);

		//Update Relations
		Clans.CUtil().updateRelations(creator);

		//Log
		Clans.log("[" + clan.GetName() + "] with Admin [" + admin + "] created by [" + creator + "].");

		return clan;
	}

	public void join(ClansClan clan, String player, Role role) 
	{
		//Client
		CoreClient client = Clans.Clients().Get(player);

		if (client.Clan().InClan())
			leave(Clans.CUtil().getClanByPlayer(player), player);

		//Update Clan
		clan.GetMembers().put(player, role);
		Clans.GetClanMemberMap().put(player, clan);
		clan.GetInviteeMap().remove(player);
		String inviter = clan.GetInviterMap().remove(player);
		if (inviter == null)	inviter = "";

		//Update Client
		client.Clan().Update(clan.GetName(), inviter, 0L);

		//Save
		Clans.CRepo().EditClan(clan.GetToken());

		//Update Relations
		Clans.CUtil().updateRelations(player);

		//Log
		Clans.log("Added [" + player + "] to [" + clan.GetName() + "].");
	}

	public void leave(ClansClan clan, String player)
	{
		if (clan == null)
			return;

		//Update Client
		CoreClient client = Clans.Clients().Get(player);
		if (client != null)
			client.Clan().Update("", "", clan.getTimer());

		//Update Clan
		clan.GetMembers().remove(player);
		Clans.GetClanMemberMap().remove(player);

		//Save
		Clans.CRepo().EditClan(clan.GetToken());

		//Update Relations
		Clans.CUtil().updateRelations(player);

		//Log
		Clans.log("Removed [" + player + "] from [" + clan.GetName() + "].");
	}

	public void role(ClansClan clan, String player, Role role)
	{
		//Update Clan
		clan.GetMembers().put(player, role);

		//Save
		Clans.CRepo().EditClan(clan.GetToken());

		//Log
		Clans.log("Removed [" + player + "] from [" + clan.GetName() + "].");
	}

	public void invite(ClansClan clan, String player, String inviter)
	{
		clan.GetInviteeMap().put(player, System.currentTimeMillis());
		clan.GetInviterMap().put(player, inviter);

		//Log
		Clans.log("Invited [" + player + "] to [" + clan.GetName() + "] by [" + inviter + "].");
	}

	public void requestAlly(ClansClan clan, ClansClan target, String player)
	{
		clan.GetRequestMap().put(target.GetName(), System.currentTimeMillis());

		//Log
		Clans.log("Alliance Request to [" + target.GetName() + "] from [" + clan.GetName() + "] by [" + player + "].");
	}

	public void ally(ClansClan cA, ClansClan cB, String player)
	{
		//Remove Requests
		cA.GetRequestMap().remove(cB.GetName());
		cB.GetRequestMap().remove(cA.GetName());

		//Update Clans
		cA.GetAllyMap().put(cB.GetName(), false);
		cB.GetAllyMap().put(cA.GetName(), false);

		//Save
		Clans.CRepo().EditClan(cA.GetToken());
		Clans.CRepo().EditClan(cB.GetToken());

		//Update Relations
		for (String cur : cA.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Update Relations
		for (String cur : cB.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Log
		Clans.log("Added Ally for [" + cB.GetName() + "] and [" + cA.GetName() + "] by [" + player + "].");
	}

	public boolean trust(ClansClan cA, ClansClan cB, String player)
	{
		if (!cA.GetAllyMap().containsKey(cB.GetName()))
			return false;

		boolean trust = !cA.GetAllyMap().get(cB.GetName());

		//Memory
		cA.GetAllyMap().put(cB.GetName(), trust);

		//Save
		Clans.CRepo().EditClan(cA.GetToken());
		Clans.CRepo().EditClan(cB.GetToken());

		//Update Relations
		for (String cur : cA.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Update Relations
		for (String cur : cB.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Log
		Clans.log("Gave Trust [" + trust + "] to [" + cB.GetName() + "] for [" + cA.GetName() + "] by [" + player + "].");

		return trust;
	}

	public void neutral(ClansClan cA, ClansClan cB, String player, boolean bothClans) 
	{
		//Update Clans
		cA.GetAllyMap().remove(cB.GetName());
		cB.GetAllyMap().remove(cA.GetName());

		cA.GetEnemyOut().remove(cB.GetName());
		cB.GetEnemyIn().remove(cA.GetName());

		if (bothClans)
		{
			cA.GetEnemyIn().remove(cB.GetName());
			cB.GetEnemyOut().remove(cA.GetName());
		}

		//Save
		Clans.CRepo().EditClan(cA.GetToken());
		Clans.CRepo().EditClan(cB.GetToken());

		//Update Relations
		for (String cur : cA.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Update Relations
		for (String cur : cB.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Log
		Clans.log("Added Neutral between [" + cA.GetName() + "] and [" + cB.GetName() + "] by [" + player + "].");
	}

	public void enemy(ClansWar war, String player)
	{
		war.GetClanA().GetEnemyOut().put(war.GetClanB().GetName(), war);
		war.GetClanB().GetEnemyIn().put(war.GetClanA().GetName(), war);

		//Save
		Clans.CRepo().EditClan(war.GetClanA().GetToken());

		//Update Relations
		for (String cur : war.GetClanA().GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Update Relations
		for (String cur : war.GetClanB().GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Log
		Clans.log("Added Invasion between [" + war.GetClanA().GetName() + "] and [" + war.GetClanB().GetName() + "] by [" + player + "].");
	}



	public boolean claim(String name, String chunk, String player, boolean safe)
	{
		if (!Clans.GetClanMap().containsKey(name))
			return false;

		ClansClan clan = Clans.GetClanMap().get(name);

		//Unclaim
		if (Clans.GetClaimMap().containsKey(chunk))
			unclaim(chunk, player, false);

		//Memory
		ClansTerritory claim = new ClansTerritory(Clans, name, chunk, safe);
		clan.GetClaimSet().add(chunk);
		Clans.GetClaimMap().put(chunk, claim);

		//Save
		Clans.CRepo().EditClan(clan.GetToken());

		//Visual
		Chunk c = UtilWorld.strToChunk(chunk);
		if (!clan.IsAdmin())
			for (int i = 0 ; i < 3 ; i++)
				for (int x=0 ; x < 16 ; x++)
					for (int z=0 ; z < 16 ; z++)
						if (z == 0 || z == 15 || x == 0 || x == 15)
						{
							Block down = UtilBlock.getHighest(c.getWorld(), c.getBlock(x, 0, z).getX(), c.getBlock(x, 0, z).getZ()).getRelative(BlockFace.DOWN);

							if (down.getTypeId() == 1 || down.getTypeId() == 2 || down.getTypeId() == 3 || down.getTypeId() == 12 || down.getTypeId() == 8)
								Clans.BlockRestore().Add(down, 89, (byte)0, 180000);
						}

		//Log
		Clans.log("Added Claim for [" + name + "] at [" + chunk + "] by [" + player + "].");

		return true;
	}

	public boolean unclaim(String chunk, String player, boolean sql)
	{
		ClansTerritory claim = Clans.GetClaimMap().remove(chunk);

		if (claim == null)
		{
			Clans.log("Unclaiming NULL Chunk Failed.");
			return false;
		}

		ClansClan clan = Clans.GetClanMap().get(claim.owner);

		if (clan == null)
		{
			Clans.log("Unclaiming from NULL Clan Failed.");
			return false;
		}

		//Memory
		clan.GetClaimSet().remove(chunk);

		//Save
		Clans.CRepo().EditClan(clan.GetToken());

		//Register
		Clans.GetUnclaimMap().put(chunk, System.currentTimeMillis());

		//Log
		Clans.log("Removed Claim for [" + clan.GetName() + "] at [" + chunk + "] by [" + player + "].");

		return true;
	}

	public void home(ClansClan clan, Location loc, String player)
	{
		//Memory
		clan.SetHome(loc);

		//Save
		Clans.CRepo().EditClan(clan.GetToken());

		//Log
		Clans.log("Set Home for [" + clan.GetName() + "] to " + UtilWorld.locToStrClean(loc) + " by [" + player + "].");
	}

	public void safe(ClansTerritory claim, String player)
	{
		//Memory
		claim.safe = !claim.safe;

		//Save
		Clans.CRepo().EditClan(Clans.getClan(claim.getOwner()).GetToken());

		//Log
		Clans.log("Safe Zone at [" + claim.chunk + "] set to [" + claim.safe + "] by [" + player + "].");
	}

	public void pillage(ClansClan cA, ClansClan cB, boolean start) 
	{
		if (start)
		{
			cA.GetEnemyEvent().add(cB.GetName());
			cB.GetEnemyEvent().add(cA.GetName());
		}
		else
		{
			cA.GetEnemyEvent().remove(cB.GetName());
			cB.GetEnemyEvent().remove(cA.GetName());
		}

		//Update Relations
		for (String cur : cA.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);

		//Update Relations
		for (String cur : cB.GetMembers().keySet())
			Clans.CUtil().updateRelations(cur);
	}
}
