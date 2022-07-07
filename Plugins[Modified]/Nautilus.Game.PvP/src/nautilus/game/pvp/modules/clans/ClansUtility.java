package nautilus.game.pvp.modules.clans;

import java.util.LinkedList;
import java.util.Map.Entry;

import nautilus.game.pvp.modules.clans.ClansClan.Role;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;

public class ClansUtility 
{
	private Clans Clans;
	public ClansUtility(Clans clans)
	{
		Clans = clans;
	}

	public enum ClanRelation
	{
		SELF,
		ALLY,
		ALLY_TRUST,
		NEUTRAL,
		ENEMY,
		PILLAGE,
		ADMIN,
		SAFE
	}

	public ClansClan searchClanPlayer(Player caller, String name, boolean inform)
	{
		//CLAN
		LinkedList<ClansClan> clanMatchList = new LinkedList<ClansClan>();

		for (ClansClan cur : Clans.GetClanMap().values())
		{
			if (cur.GetName().equalsIgnoreCase(name))
				return cur;

			if (cur.GetName().toLowerCase().contains(name.toLowerCase()))
				clanMatchList.add(cur);
		}

		if (clanMatchList.size() == 1)
			return clanMatchList.get(0);

		//No / Non-Unique
		String clanMatchString = "None";
		if (clanMatchList.size() > 1)
		{
			for (ClansClan cur : clanMatchList)
				clanMatchString += cur.GetName() + " ";
		}

		//PLAYER
		LinkedList<ClansClan> playerMatchList = new LinkedList<ClansClan>();

		for (Entry<String, ClansClan> clanMemberEntry : Clans.GetClanMemberMap().entrySet())
		{
			if (clanMemberEntry.getKey().equalsIgnoreCase(name))
				return clanMemberEntry.getValue();

			if (clanMemberEntry.getKey().toLowerCase().contains(name.toLowerCase()))
				playerMatchList.add(clanMemberEntry.getValue());
		}

		if (playerMatchList.size() == 1)
			return playerMatchList.get(0);

		//No / Non-Unique
		String playerMatchString = "None";
		if (playerMatchList.size() > 1)
		{
			for (ClansClan cur : playerMatchList)
				playerMatchString += cur.GetName() + " ";
		}

		if (inform)
		{
			UtilPlayer.message(caller, F.main("Clan Search", "" +
					C.mCount + (clanMatchList.size() + playerMatchList.size()) +
					C.mBody + " matches for [" +
					C.mElem + name +
					C.mBody + "]."), false);

			UtilPlayer.message(caller, F.desc("Matches via Clan", clanMatchString), false);
			UtilPlayer.message(caller, F.desc("Matches via Player", playerMatchString), false);;
		}

		return null;
	}

	public ClansClan searchClan(Player caller, String name, boolean inform)
	{
		LinkedList<ClansClan> matchList = new LinkedList<ClansClan>();

		for (ClansClan cur : Clans.GetClanMap().values())
		{
			if (cur.GetName().equalsIgnoreCase(name))
				return cur;

			if (cur.GetName().toLowerCase().contains(name.toLowerCase()))
				matchList.add(cur);
		}

		//No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform)
				return null;

			//Inform
			UtilPlayer.message(caller, F.main("Clan Search", "" +
					C.mCount + matchList.size() +
					C.mBody + " matches for [" +
					C.mElem + name +
					C.mBody + "]."), false);

			if (matchList.size() > 0)
			{
				String matchString = "";
				for (ClansClan cur : matchList)
					matchString += cur.GetName() + " ";

				UtilPlayer.message(caller, F.main("Clan Search", "" +
						C.mBody + " Matches [" +
						C.mElem + matchString +
						C.mBody + "]."), false);
			}

			return null;
		}

		return matchList.get(0);
	}

	public ClansClan getClanByClanName(String clan)
	{
		return Clans.getClan(clan);
	}

	public ClansClan getClanByPlayer(Player player)
	{
		return getClanByPlayer(player.getName());
	}

	public ClansClan getClanByPlayer(String name)
	{
		if (!Clans.GetClanMemberMap().containsKey(name))
			return null;

		return Clans.GetClanMemberMap().get(name);
	}

	public Role getRole(Player player) 
	{
		try
		{
			return getClanByPlayer(player).GetMembers().get(player.getName());
		}
		catch (Exception e)
		{
			return Role.NONE;
		}	
	}

	public boolean isSafe(Player player)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(player);
		if (clan != null)
		{
			if (!clan.GetEnemyEvent().isEmpty())
				return false;
		}
		
		if (!UtilTime.elapsed(Clans.Clients().Get(player).Player().GetLastDamager(), 15000))
			return false;

		return isSafe(player.getLocation());
	}

	public boolean isSafe(Location loc)
	{
		/* XXX
		for (Region cur : _clans.regionSet)
		{
			if (cur.contains(loc))
				return true;
		}
		 */

		if (!Clans.GetClaimMap().containsKey(UtilWorld.chunkToStr(loc.getChunk())))
			return false;

		return Clans.GetClaimMap().get(UtilWorld.chunkToStr(loc.getChunk())).safe;
	}

	public boolean isChunkHome(ClansClan clan, Chunk chunk)
	{
		if (clan == null)
			return false;

		if (clan.GetHome() == null)
			return false;

		return clan.GetHome().getChunk().equals(chunk);
	}

	public ClansTerritory getClaim(Location loc)
	{
		String chunk = UtilWorld.chunkToStr(loc.getChunk());
		return Clans.GetClaimMap().get(chunk);
	}

	public ClansTerritory getClaim(String chunk)
	{
		return Clans.GetClaimMap().get(chunk);
	}

	public ClansClan getOwner(String chunk)
	{
		ClansTerritory claim = getClaim(chunk);

		if (claim == null)
			return null;

		return getOwner(claim);
	}

	public ClansClan getOwner(Location loc)
	{
		ClansTerritory claim = getClaim(loc);

		if (claim != null)
			return getOwner(claim);

		for (ClansOutpost outpost : Clans.GetOutpostMap().values())
			if (outpost.IsOn())
				if (outpost.Contains(loc))
					return outpost.GetClan();

		return null;
	}

	public ClansClan getOwner(ClansTerritory claim)
	{
		return getClanByClanName(claim.owner);
	}

	public String getOwnerString(Location loc)
	{
		ClansClan owner = getOwner(loc);

		if (owner == null)
			return "Wilderness";

		return owner.GetName();
	}

	public String getOwnerStringRel(Location loc, String player)
	{
		ClanRelation rel = Clans.CUtil().relPT(player, UtilWorld.chunkToStr(loc.getChunk()));
		return Clans.CUtil().mRel(rel, Clans.CUtil().getOwnerString(loc), true);
	}

	public boolean isClaimed(Location loc)
	{
		String chunk = UtilWorld.chunkToStr(loc.getChunk());

		return Clans.GetClaimMap().containsKey(chunk);
	}

	public boolean isAlliance(String player, Location loc)
	{
		if (!Clans.Clients().Get(player).Clan().InClan())
			return false;

		if (!isClaimed(loc))
			return false;

		return getOwner(getClaim(loc)).isAlly(Clans.Clients().Get(player).Clan().GetClanName());
	}

	public boolean isSelf(String player, Location loc)
	{
		if (!Clans.Clients().Get(player).Clan().InClan())
			return false;

		if (!isClaimed(loc))
			return false;

		return getOwner(getClaim(loc)).isSelf(Clans.Clients().Get(player).Clan().GetClanName());
	}

	public boolean isAdmin(Location loc)
	{
		if (!isClaimed(loc))
			return false;

		return getOwner(getClaim(loc)).IsAdmin();
	}

	public boolean isSpecial(Location loc, String special)
	{
		if (!isClaimed(loc))
			return false;

		if (!isAdmin(loc))
			return false;

		return getOwner(getClaim(loc)).GetName().toLowerCase().contains(special.toLowerCase());
	}

	public ClanRelation getAccess(Player player, Location loc)
	{
		//Observer Override
		if (Clans.Observer().isObserver(player, false))
		{
			if (Clans.Observer().isObserver(player, true))
				return ClanRelation.SELF;
			else
				return ClanRelation.NEUTRAL;		
		}

		ClansClan owner = this.getOwner(loc);
		ClansClan clan = getClanByPlayer(player);

		if (owner == null)
			return ClanRelation.SELF;

		if (owner.equals(Clans.CAdmin().getMimic(player, false)))
			return ClanRelation.SELF;

		if (owner.equals(clan))
			return ClanRelation.SELF;

		if (clan != null)
			if (clan.canPillage(owner))
				return ClanRelation.SELF;

		if (clan != null)
			if (owner.getTrust(clan.GetName()))
				return ClanRelation.ALLY_TRUST;

		if (clan != null)
			if (owner.isAlly(clan.GetName()))
				return ClanRelation.ALLY;

		if (clan != null)
			if (owner.isEnemy(clan.GetName()))
				return ClanRelation.ENEMY;

		return ClanRelation.NEUTRAL;
	}

	//Player Player
	public ClanRelation relPP(String pA, String pB) 
	{
		return rel(getClanByPlayer(pA), getClanByPlayer(pB));
	}

	//Clan Clan
	public ClanRelation relCC(String cA, String cB) 
	{
		return rel(searchClan(null, cA, false), searchClan(null, cB, false));
	}

	//Territory Territory
	public ClanRelation relTT(String tA, String tB) 
	{
		return rel(getOwner(tA), getOwner(tB));
	}

	//Player Clan
	public ClanRelation relPC(String pA, String cB) 
	{
		return rel(getClanByPlayer(pA), searchClan(null, cB, false));
	}

	//Player Clan (Object)
	public ClanRelation relPC(String pA, ClansClan cB) 
	{
		return rel(getClanByPlayer(pA), cB);
	}

	//Player Territory
	public ClanRelation relPT(String pA, String tB) 
	{
		ClansTerritory claim = getClaim(tB);
		if (claim != null)
			if (claim.safe)
				return ClanRelation.SAFE;

		return rel(getClanByPlayer(pA), getOwner(tB));
	}

	//Clan Territory
	public ClanRelation relCT(String cA, String tB) 
	{
		ClansTerritory claim = getClaim(tB);
		if (claim != null)
			if (claim.safe)
				return ClanRelation.SAFE;

		return rel(searchClan(null, cA, false), getOwner(tB));
	}

	public ClanRelation rel(ClansClan cA, ClansClan cB) 
	{
		if (cA == null || cB == null)				
			return ClanRelation.NEUTRAL;

		//Self
		if (cA.IsAdmin() || cB.IsAdmin())	
			return ClanRelation.ADMIN;

		if (cA.GetName().equals(cB.GetName()))	
			return ClanRelation.SELF;

		//Ally
		if (cA.getTrust(cB.GetName()))
			return ClanRelation.ALLY_TRUST;

		if (cA.isAlly(cB.GetName()))
			return ClanRelation.ALLY;

		if (cA.isPillage(cB.GetName()))
			return ClanRelation.PILLAGE;
		
		if (cA.isEnemy(cB.GetName()))
			return ClanRelation.ENEMY;

		//Enemy
		return ClanRelation.NEUTRAL;
	}

	public ChatColor relChatColor(ClanRelation relation, boolean dark)
	{
		if (relation == ClanRelation.SAFE)				return C.xAdmin;
		if (relation == ClanRelation.ADMIN)				return C.xAdmin;

		if (!dark)
		{	
			if (relation == ClanRelation.SELF)			return C.xSelf;
			if (relation == ClanRelation.ALLY_TRUST)	return C.xdAlly;
			if (relation == ClanRelation.ALLY)			return C.xAlly;
			if (relation == ClanRelation.ENEMY)			return C.xWar; 
			if (relation == ClanRelation.PILLAGE)		return C.xPillage; 
			return C.xEnemy;
		}

		if (relation == ClanRelation.SELF)				return C.xdSelf;
		if (relation == ClanRelation.ALLY_TRUST)		return C.xAlly;
		if (relation == ClanRelation.ALLY)				return C.xdAlly;
		if (relation == ClanRelation.ENEMY)				return C.xdWar;
		if (relation == ClanRelation.PILLAGE)			return C.xdPillage;
		return C.xdEnemy;
	}

	public String relColor(ClanRelation relation, boolean dark)
	{
		if (relation == ClanRelation.SAFE)				return C.xAdmin + "(" + C.xSafe + "SAFE" + C.xAdmin + ") ";

		return relChatColor(relation, dark) + "";
	}

	public String mRel(ClanRelation relation, String message, boolean dark)
	{
		return relColor(relation, dark) + message + C.mChat;
	}

	public boolean playerSelf(String pA, String pB)
	{
		ClansClan cA = getClanByPlayer(pA);
		ClansClan cB = getClanByPlayer(pB);

		if (cA == null || cB == null)				
			return false;

		return cA.isSelf(cB.GetName());
	}

	public boolean playerAlly(String pA, String pB)
	{
		ClansClan cA = getClanByPlayer(pA);
		ClansClan cB = getClanByPlayer(pB);

		if (cA == null || cB == null)				
			return false;

		return cA.isAlly(cB.GetName());
	}

	public boolean playerEnemy(String pA, String pB)
	{
		ClansClan cA = getClanByPlayer(pA);
		ClansClan cB = getClanByPlayer(pB);

		if (cA == null || cB == null)				
			return true;

		return !(cA.isAlly(cB.GetName()) || cA.isSelf(cB.GetName()));
	}

	public boolean canHurt(Player damagee, Player damager)
	{
		if (damagee == null)
			return false;

		if (isSafe(damagee))
			return false;
		
		if (damager == null)
			return true;		

		if (isSafe(damager))
			return false;

		ClanRelation rel = Clans.CUtil().relPP(damagee.getName(), damager.getName());

		if (rel == ClanRelation.ALLY || rel == ClanRelation.ALLY_TRUST || rel == ClanRelation.SELF)
			return false;

		return true;
	}

	public void updateRelations(String name) 
	{
		updateRelations(UtilPlayer.searchExact(name));
	}

	public void updateRelations(Player player) 
	{
		if (player == null)
			return;

		CoreClient client = Clans.Clients().Get(player);

		for (Player cur : UtilServer.getPlayers())
		{
			//For Player
			client.Clan().SetRelationship(
					cur.getName(), Clans.CUtil().relPP(cur.getName(), player.getName()));

			//For Other
			Clans.Clients().Get(cur.getName()).Clan().SetRelationship(
					player.getName(), Clans.CUtil().relPP(player.getName(), cur.getName()));

			if (player.canSee(cur))
			{
				player.hidePlayer(cur);
				player.showPlayer(cur);
			}

			if (cur.canSee(player))
			{
				cur.hidePlayer(player);
				cur.showPlayer(player);
			}
		}	
	}
	
	public boolean isBorderlands(Location loc)
	{
		return (Math.abs(loc.getX()) > 400 || Math.abs(loc.getZ()) > 400);
	}
}
