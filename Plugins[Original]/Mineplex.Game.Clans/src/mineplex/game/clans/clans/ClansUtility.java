package mineplex.game.clans.clans;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.event.ClanDisbandedEvent;
import mineplex.game.clans.clans.event.PlayerClaimTerritoryEvent;
import mineplex.game.clans.clans.event.PlayerPreClaimTerritoryEvent;
import mineplex.game.clans.clans.event.PlayerUnClaimTerritoryEvent;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.spawn.Spawn;

public class ClansUtility
{
	// The maximum number of clans to search before exiting early. Inclusive
	private static final int MAX_CLAN_SEARCH = 10;
	// The maximum number of players to search before exiting early. Inclusive
	private static final int MAX_PLAYER_SEARCH = 10;

	private ClansManager _clansManager;
	
	public ClansUtility(ClansManager clans)
	{
		_clansManager = clans;
	}
	
	public enum ClanRelation
	{
		SELF(C.xSelf, C.xdSelf),
		ALLY(C.xAlly, C.xdAlly),
		ALLY_TRUST(C.xAlly, C.xdAlly),
		WAR_WINNING(C.xWarWinning, C.xdWarWinning),
		WAR_LOSING(C.xWarLosing, C.xdWarLosing),
		NEUTRAL(C.xNeutral, C.xdNeutral),
		ADMIN(C.xAdmin, C.xAdmin),
		SAFE(C.xSafe, C.xSafe);
		
		private ChatColor _light;
		private ChatColor _dark;
		
		ClanRelation(ChatColor light, ChatColor dark)
		{
			_light = light;
			_dark = dark;
		}
		
		public String getPrefix()
		{
			return _light.toString();
		}
		
		public ChatColor getColor(boolean dark)
		{
			return dark ? _dark : _light;
		}
	}
	
	/**
	 * 
	 * @param location
	 * @param radius
	 * @return a 2D array of {@link ClanTerritory} with uniform dimension of (
	 *         {@code radius} * 2 + 1). The region represented by the array of
	 *         territories is centered on {@code location} chunk with a given
	 *         chunk {@code radius}.
	 */
	public List<ChunkData> getTerritory(Location location, int radius, ClanInfo surveyorClan)
	{
		World world = location.getWorld();
		Chunk chunk = location.getChunk();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int width = radius * 2 + 1;
		
		List<ChunkData> chunks = new ArrayList<ChunkData>();
		
		for (int x = 0; x < width; x++)
		{
			for (int z = 0; z < width; z++)
			{
				int territoryX = chunkX - radius + x;
				int territoryZ = chunkZ - radius + z;
				ClanTerritory territory = getClaim(world.getChunkAt(territoryX, territoryZ));
				
				if (territory != null)
				{
					ClanInfo clan = getOwner(territory);
					String clanName = territory.Owner;
					ClanRelation relationship = rel(surveyorClan, clan);
					ChatColor color = relChatColor(relationship, false);
					
					ChunkData data = new ChunkData(territoryX, territoryZ, color, clanName);
					
					chunks.add(data);
				}
			}
		}
		
		return chunks;
	}
	
	public ClanInfo searchClanPlayer(Player caller, String name, boolean inform)
	{
		// CLAN
		List<ClanInfo> clanMatchList = new ArrayList<>(MAX_CLAN_SEARCH);

		for (ClanInfo cur : _clansManager.getClanMap().values())
		{
			if (cur.getName().equalsIgnoreCase(name)) return cur;

			if (cur.getName().toLowerCase().contains(name.toLowerCase())) clanMatchList.add(cur);

			if (clanMatchList.size() > MAX_CLAN_SEARCH) break;
		}

		if (clanMatchList.size() == 1) return clanMatchList.get(0);
		
		// PLAYER
		List<ClanInfo> playerMatchList = new ArrayList<>(MAX_PLAYER_SEARCH);

		outer: for (ClanInfo clanInfo : _clansManager.getClanMap().values())
		{
			for (ClansPlayer player : clanInfo.getMembers().values())
			{
				if (player.getPlayerName().equalsIgnoreCase(name)) return clanInfo;

				if (player.getPlayerName().toLowerCase().contains(name.toLowerCase()))
				{
					playerMatchList.add(clanInfo);
					// No duplicate results please
					continue outer;
				}

				if (playerMatchList.size() > MAX_PLAYER_SEARCH) break outer;
			}
		}

		if (playerMatchList.size() == 1) return playerMatchList.get(0);

		if (inform)
		{
			UtilPlayer.message(caller, F.main("Clan Search", "" + C.mCount + (clanMatchList.size() + playerMatchList.size()) + C.mBody + " matches for [" + C.mElem + name + C.mBody + "]."));

			if (clanMatchList.size() > MAX_CLAN_SEARCH)
			{
				UtilPlayer.message(caller, F.main("Clan Search", "Too many clans matched. Try a more specific search"));
			}
			else if (clanMatchList.size() == 0)
			{
				UtilPlayer.message(caller, F.main("Clan Search", "No clans matched. Try a more specific search"));
			}
			else
			{
				StringBuilder clanMatchString = new StringBuilder();
				for (ClanInfo clanInfo : clanMatchList)
				{
					clanMatchString.append(clanInfo.getName()).append(" ");
				}
				UtilPlayer.message(caller, F.desc("Matches via Clan", clanMatchString.toString()));
			}
			if (playerMatchList.size() > MAX_PLAYER_SEARCH)
			{
				UtilPlayer.message(caller, F.main("Clan Search", "Too many players matched. Try a more specific search"));
			}
			else if (playerMatchList.size() == 0)
			{
				UtilPlayer.message(caller, F.main("Clan Search", "No players matched. Try a more specific search"));
			}
			else
			{
				StringBuilder playerMatchString = new StringBuilder();
				for (ClanInfo clanInfo : playerMatchList)
				{
					playerMatchString.append(clanInfo.getName()).append(" ");
				}
				UtilPlayer.message(caller, F.desc("Matches via Player", playerMatchString.toString()));
			}
		}
		
		return null;
	}
	
	public ClanInfo searchClan(Player caller, String name, boolean inform)
	{
		LinkedList<ClanInfo> matchList = new LinkedList<ClanInfo>();
		
		for (ClanInfo cur : _clansManager.getClanMap().values())
		{
			if (cur.getName().equalsIgnoreCase(name)) return cur;
			
			if (cur.getName().toLowerCase().contains(name.toLowerCase())) matchList.add(cur);
		}
		
		// No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform) return null;
			
			// Inform
			UtilPlayer.message(caller, F.main("Clan Search", "" + C.mCount + matchList.size() + C.mBody + " matches for [" + C.mElem + name + C.mBody + "]."));
			
			if (matchList.size() > 0)
			{
				String matchString = "";
				for (ClanInfo cur : matchList)
					matchString += cur.getName() + " ";
					
				UtilPlayer.message(caller, F.main("Clan Search", "" + C.mBody + " Matches [" + C.mElem + matchString + C.mBody + "]."));
			}
			
			return null;
		}
		
		return matchList.get(0);
	}
	
	public ClanInfo getClanByClanName(String clan)
	{
		return _clansManager.getClan(clan);
	}
	
	public ClanInfo getClanById(int id)
	{
		for (ClanInfo clan : _clansManager.getClanMap().values())
		{
			if (clan.getId() == id)
			{
				return clan;
			}
		}
		
		return null;
	}
	
	public ClanInfo getClanByPlayer(Player player)
	{
		return getClanByUUID(player.getUniqueId());
	}
	
	public ClanInfo getClanByUUID(UUID uuid)
	{
		return _clansManager.getClanMemberUuidMap().get(uuid);
	}
	
	// @Deprecated
	// public ClanInfo getClanByPlayer(String name)
	// {
	// if (!Clans.getClanMemberMap().containsKey(name))
	// return null;
	//
	// return Clans.getClanMemberMap().get(name);
	// }
	
	public ClanRole getRole(Player player)
	{
		try
		{
			return getClanByPlayer(player).getMembers().get(player.getUniqueId()).getRole();
		}
		catch (Exception e)
		{
			return ClanRole.NONE;
		}
	}
	
	public boolean isSafe(Player player)
	{
		if (!UtilTime.elapsed(_clansManager.getCombatManager().getLog(player).GetLastCombatEngaged(), Spawn.COMBAT_TAG_DURATION)) return false;
		
		return isSafe(player.getLocation());
	}
	
	public boolean isSafe(Location loc)
	{
		if (_clansManager.getNetherManager().getNetherWorld().equals(loc.getWorld()))
		{
			if (_clansManager.getNetherManager().isInSpawn(loc))
			{
				return true;
			}
		}
		// Fix for PC-279
		// Do not change to getChunk
		// PlayerList#updatePlayers -> iterator -> PlayerVelocityEvent -> getChunk -> loadChunk -> loadPersistentEntities -> addTracker -> ITERATE ON SAME SET
		int chunkX = loc.getBlockX() >> 4;
		int chunkZ = loc.getBlockZ() >> 4;
		ClaimLocation claim = ClaimLocation.of(loc.getWorld().getName(), chunkX, chunkZ);
		if (!_clansManager.getClaimMap().containsKey(claim)) return false;
		
		return _clansManager.getClaimMap().get(claim).isSafe(loc);
	}
	
	public boolean isChunkHome(ClanInfo clan, Chunk chunk)
	{
		if (clan == null) return false;
		
		if (clan.getHome() == null) return false;
		
		return clan.getHome().getChunk().equals(chunk);
	}
	
	public ClanTerritory getClaim(Chunk chunk)
	{
		ClaimLocation chunkTag = ClaimLocation.of(chunk);
		return _clansManager.getClaimMap().get(chunkTag);
	}
	
	/**
	 * Get formatted name of display as shown for clanB
	 */
	public String name(ClanInfo display, ClanInfo clanB)
	{
		return rel(display, clanB).getPrefix() + display.getName();
	}
	
	public ClanTerritory getClaim(Location loc)
	{
		return getClaim(loc.getChunk());
	}
	
	public ClanTerritory getClaim(ClaimLocation location)
	{
		return _clansManager.getClaimMap().get(location);
	}
	
	public ClanInfo getOwner(ClaimLocation chunk)
	{
		ClanTerritory claim = getClaim(chunk);
		
		if (claim == null) return null;
		
		return getOwner(claim);
	}
	
	public ClanInfo getOwner(Location loc)
	{
		ClanTerritory claim = getClaim(loc);
		
		if (claim != null) return getOwner(claim);
		
		return null;
	}
	
	public boolean isNearAdminClaim(Location location)
	{
		for (int xOffset = -1; xOffset <= 1; xOffset++)
		{
			for (int zOffset = -1; zOffset <= 1; zOffset++)
			{
				if (xOffset == 0 && zOffset == 0) continue;
				
				ClaimLocation other = ClaimLocation.of(location.getWorld().getChunkAt(location.getChunk().getX() + xOffset, location.getChunk().getZ() + zOffset));
				
				ClanInfo adjClan = getOwner(other);
				
				if (adjClan != null && adjClan.isAdmin() && !adjClan.getName().equalsIgnoreCase("Wilderness"))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public ClanInfo getOwner(ClanTerritory claim)
	{
		return getClanByClanName(claim.Owner);
	}
	
	public String getOwnerString(Location loc)
	{
		ClanInfo owner = getOwner(loc);
		
		if (owner == null) return "Wilderness";
		
		return owner.getName();
	}
	
	public String getOwnerStringRel(Location loc, Player player)
	{
		ClanRelation rel = relPT(player, ClaimLocation.of(loc.getChunk()));
		return mRel(rel, getOwnerString(loc), true);
	}
	
	public boolean isClaimed(Location loc)
	{
		ClaimLocation chunk = ClaimLocation.of(loc.getChunk());
		
		return _clansManager.getClaimMap().containsKey(chunk);
	}
	
	// public boolean isAlliance(String player, Location loc)
	// {
	// if (!Clans.getClanMemberMap().containsKey(player))
	// return false;
	//
	// if (!isClaimed(loc))
	// return false;
	//
	// return
	// getOwner(getClaim(loc)).isAlly(Clans.getClanMemberMap().get(player).getName());
	// }
	
	public boolean isSelf(Player player, Location loc)
	{
		ClanInfo clan = _clansManager.getClan(player);
		
		if (clan == null) return false;
		
		if (!isClaimed(loc)) return false;
		
		return getOwner(getClaim(loc)).isSelf(clan.getName());
	}
	
	public boolean isAdmin(Location loc)
	{
		if (!isClaimed(loc)) return false;
		
		return getOwner(getClaim(loc)).isAdmin();
	}
	
	public boolean isSpecial(Location loc, String special)
	{
		if (!isClaimed(loc)) return false;
		
		if (!isAdmin(loc)) return false;
		
		return getOwner(getClaim(loc)).getName().toLowerCase().contains(special.toLowerCase());
	}
	
	public ClanRelation getAccess(Player player, Location loc)
	{
		ClanInfo owner = getOwner(loc);
		ClanInfo clan = getClanByPlayer(player);
		
		String mimic = _clansManager.Get(player).getMimic();
		
		if (mimic.length() != 0) clan = _clansManager.getClanUtility().searchClanPlayer(player, mimic, false);
		
		if (owner == null) return ClanRelation.SELF;
		
		if (owner.equals(clan)) return ClanRelation.SELF;
		
		if (clan != null) if (owner.getTrust(clan.getName())) return ClanRelation.ALLY_TRUST;
		
		if (clan != null) if (owner.isAlly(clan.getName())) return ClanRelation.ALLY;
		
		return ClanRelation.NEUTRAL;
	}
	
	// Player Player
	public ClanRelation relPP(Player pA, Player pB)
	{
		return rel(getClanByPlayer(pA), getClanByPlayer(pB));
	}
	
	// Clan Clan
	public ClanRelation relCC(String cA, String cB)
	{
		return rel(searchClan(null, cA, false), searchClan(null, cB, false));
	}
	
	// Territory Territory
	public ClanRelation relTT(ClaimLocation tA, ClaimLocation tB)
	{
		return rel(getOwner(tA), getOwner(tB));
	}
	
	// Player Clan
	public ClanRelation relPC(Player pA, String cB)
	{
		return rel(getClanByPlayer(pA), searchClan(null, cB, false));
	}
	
	// Player Clan (Object)
	public ClanRelation relPC(Player pA, ClanInfo cB)
	{
		return rel(getClanByPlayer(pA), cB);
	}
	
	// Player Territory
	public ClanRelation relPT(Player player, ClaimLocation territory)
	{
		ClanTerritory claim = getClaim(territory);
		if (claim != null && claim.isSafe(player.getLocation()))
		{
			return ClanRelation.SAFE;
		}
		
		return rel(getClanByPlayer(player), getOwner(territory));
	}
	
	// Clan Territory
	public ClanRelation relCT(String cA, ClaimLocation tB)
	{
		ClanTerritory claim = getClaim(tB);
		if (claim != null) if (claim.Safe) return ClanRelation.SAFE;
		
		return rel(searchClan(null, cA, false), getOwner(tB));
	}
	
	public ClanRelation rel(ClanInfo cA, ClanInfo cB)
	{
		if (cA == null || cB == null) return ClanRelation.NEUTRAL;
		
		// Self
		if (cA.isAdmin() || cB.isAdmin()) return ClanRelation.ADMIN;
		
		if (cA.getName().equals(cB.getName())) return ClanRelation.SELF;
		
		// Ally
		if (cA.getTrust(cB.getName())) return ClanRelation.ALLY_TRUST;
		
		if (cA.isAlly(cB.getName())) return ClanRelation.ALLY;
		
		// War
		int warPoints = cA.getWarPoints(cB);
		if (warPoints >= 10)
			return ClanRelation.WAR_WINNING;
		else if (warPoints <= -10) return ClanRelation.WAR_LOSING;
		
		return ClanRelation.NEUTRAL;
	}
	
	public ChatColor relChatColor(ClanRelation relation, boolean dark)
	{
		return relation.getColor(dark);
	}
	
	public String relColor(ClanRelation relation, boolean dark)
	{
		if (relation == ClanRelation.SAFE) return C.xAdmin + "(" + C.xSafe + "SAFE" + C.xAdmin + ") ";
		
		return relChatColor(relation, dark) + "";
	}
	
	public String mRel(ClanRelation relation, String message, boolean dark)
	{
		return relColor(relation, dark) + message + C.mChat;
	}
	
	public boolean playerSelf(Player pA, Player pB)
	{
		ClanInfo cA = getClanByPlayer(pA);
		ClanInfo cB = getClanByPlayer(pB);
		
		if (cA == null || cB == null) return false;
		
		return cA.isSelf(cB.getName());
	}
	
	public boolean playerAlly(Player pA, Player pB)
	{
		ClanInfo cA = getClanByPlayer(pA);
		ClanInfo cB = getClanByPlayer(pB);
		
		if (cA == null || cB == null) return false;
		
		return cA.isAlly(cB.getName());
	}
	
	public boolean playerEnemy(Player pA, Player pB)
	{
		ClanInfo cA = getClanByPlayer(pA);
		ClanInfo cB = getClanByPlayer(pB);
		
		if (cA == null || cB == null) return true;
		
		return !(cA.isAlly(cB.getName()) || cA.isSelf(cB.getName()));
	}
	
	public boolean canHurt(Player damagee, Player damager)
	{
		if (damagee == null || damager == null) return false;
		
		ClanRelation rel = relPP(damagee, damager);
		
		if (rel == ClanRelation.ALLY || rel == ClanRelation.ALLY_TRUST || rel == ClanRelation.SELF) return false;
		
		return true;
	}
	
	public boolean isBorderlands(Location loc)
	{
		return (Math.abs(loc.getBlockX()) > ClansManager.CLAIMABLE_RADIUS || Math.abs(loc.getBlockZ()) > ClansManager.CLAIMABLE_RADIUS) && loc.getWorld().getName().equalsIgnoreCase("world");
	}
	
	/**
	 * Used for commands
	 */
	
	public void join(final Player caller, final ClanInfo clanInfo)
	{
		if (_clansManager.getClanMemberUuidMap().containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already in a Clan."));
			return;
		}
		
		if (_clansManager.leftRecently(caller.getUniqueId(), 20 * 60 * 1000) != null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot join a Clan for " + C.mTime + UtilTime.MakeStr(_clansManager.leftRecently(caller.getUniqueId(), 20 * 60 * 1000).getRight()) + C.mBody + "."));
			return;
		}
		
		if (!_clansManager.Get(caller).canJoin())
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot join a Clan for " + C.mTime + UtilTime.convertString(System.currentTimeMillis() - _clansManager.Get(caller).getDelay(), 1, UtilTime.TimeUnit.FIT) + C.mBody + "."));
			return;
		}
		
		if (clanInfo == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "Error: Clan does not exist"));
			return;
		}
		
		if (!clanInfo.isInvited(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not invited to " + F.elem("Clan " + clanInfo.getName()) + "."));
			return;
		}
		
		if (clanInfo.getSize() >= clanInfo.getMaxSize())
		{
			UtilPlayer.message(caller, F.main("Clans", "The clan " + F.elem("Clan " + clanInfo.getName()) + " is full and cannot be joined!"));
			return;
		}
		
		// Task
		_clansManager.getClanDataAccess().join(clanInfo, caller, ClanRole.RECRUIT, new Callback<Boolean>()
		{
			@Override
			public void run(Boolean data)
			{
				if (data)
				{
					// Inform
					UtilPlayer.message(caller, F.main("Clans", "You joined " + F.elem("Clan " + clanInfo.getName()) + "."));
					clanInfo.inform(F.name(caller.getName()) + " has joined your Clan.", caller.getName());
				}
				else
				{
					UtilPlayer.message(caller, F.main("Clans", "There was an error processing your request"));
				}
			}
		});
		
	}
	
	public void leave(final Player caller)
	{
		final ClanInfo clan = getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		if (clan.getMembers().get(caller.getUniqueId()).getRole() == ClanRole.LEADER && clan.getMembers().size() > 1)
		{
			UtilPlayer.message(caller, F.main("Clans", "You must pass on " + F.elem("Leadership") + " before leaving."));
			return;
		}
		
		// Leave or Delete
		if (clan.getMembers().size() > 1)
		{
			// Task
			_clansManager.getClanDataAccess().leave(clan, caller, new Callback<Boolean>()
			{
				@Override
				public void run(Boolean data)
				{
					// Inform
					UtilPlayer.message(caller, F.main("Clans", "You left " + F.elem("Clan " + clan.getName()) + "."));
					clan.inform(F.name(caller.getName()) + " has left your Clan.", null);
					
					clan.left(caller.getName());
				}
			});
		}
		else
		{
			delete(caller);
		}
	}
	
	public void delete(final Player caller)
	{
		final ClanInfo clan = getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		if (getRole(caller) != ClanRole.LEADER)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader can disband the Clan."));
			return;
		}
		
		// Event
		ClanDisbandedEvent event = new ClanDisbandedEvent(clan, caller);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		_clansManager.messageClan(clan, F.main("Clans", C.cYellow + caller.getName() + C.mBody + " has disbanded the Clan."));
		
		// Task
		_clansManager.getClanDataAccess().delete(clan, new Callback<Boolean>()
		{
			@Override
			public void run(Boolean data)
			{
				if (!data)
				{
					UtilPlayer.message(caller, F.main("Clans", "There was an error processing your request. Try again later"));
				}
				else
				{
					UtilPlayer.message(caller, F.main("Clans", "You disbanded your Clan."));
				}
			}
		});
	}
	
	public boolean claim(Player caller)
	{
		ClanInfo clan = getClanByPlayer(caller);
		
		// Pre Event
		PlayerPreClaimTerritoryEvent preEvent = new PlayerPreClaimTerritoryEvent(caller, caller.getLocation().getChunk(), clan);
		
		UtilServer.getServer().getPluginManager().callEvent(preEvent);
		
		if (preEvent.isCancelled())
		{
			return false;
		}
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return false;
		}
		
		if (clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER && clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can claim Territory."));
			return false;
		}
		
		if (_clansManager.getNetherManager().isInNether(caller))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot claim territory while in " + F.clansNether("The Nether") + "!"));
		}
		
		if (!ClansManager.isClaimable(caller.getLocation()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot claim territory at this location!"));
			return false;
		}
		
		if (clan.getEnergy() == 0)
		{
			UtilPlayer.message(caller, F.main("Clans", "You must purchase energy at a shop before you can claim land."));
			return false;
		}
		
		ClaimLocation chunk = ClaimLocation.of(caller.getLocation().getChunk());
		ClanInfo ownerClan = getOwner(caller.getLocation());
		
		// Try to Steal
		if (ownerClan != null && !ownerClan.equals(clan))
		{
			if (unclaimSteal(caller, clan, ownerClan))
			{
				return true;
			}
			else
			{
				UtilPlayer.message(caller, F.main("Clans", "This Territory is owned by " + mRel(_clansManager.getClanUtility().relPC(caller, ownerClan), ownerClan.getName(), true) + "."));
				return false;
			}
		}
		
		 if (clan.getClaims() >= clan.getClaimsMax())
		 {
			 UtilPlayer.message(caller, F.main("Clans", "Your Clan cannot claim more Territory."));
			 return false;
		 }
		
		// Adjacent
		boolean selfAdj = false;
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				if ((x == 1 && z == 1)
				 || (x == -1 && z == 1)
				 || (x == -1 && z == -1)
				 || (x == 1 && z == -1)
				 || (x == 0 && z == 0)) {
					continue;
				}
				
				ClaimLocation other = ClaimLocation.of(caller.getWorld().getChunkAt(caller.getLocation().getChunk().getX() + x, caller.getLocation().getChunk().getZ() + z));
				
				ClanInfo adjClan = getOwner(other);
				
				if (adjClan == null) continue;
				
				if (checkBox(caller.getWorld().getChunkAt(caller.getLocation().getChunk().getX() + x, caller.getLocation().getChunk().getZ() + z), 3))
				{
					UtilPlayer.message(caller, F.main("Clans", "You cannot claim this Territory, it causes a box."));
					UtilPlayer.message(caller, F.main("Clans", "This means a Territory has all sides claimed."));
					return false;
				}
				
				if (rel(clan, adjClan) == ClanRelation.SELF)
				{
					selfAdj = true;
				}
				else if (rel(clan, adjClan) != ClanRelation.SELF)
				{
					UtilPlayer.message(caller, F.main("Clans", "You cannot claim Territory next to " + mRel(rel(ownerClan, adjClan), adjClan.getName(), true) + "."));
					return false;
				}
			}
		}
		
		// Boxed
		if (checkBox(caller.getLocation().getChunk(), 4))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot claim this Territory, it causes a box."));
			UtilPlayer.message(caller, F.main("Clans", "This means a Territory has all sides claimed."));
			return false;
		}
		
		if (isNearAdminClaim(caller.getLocation()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot claim so close to administrative territory!"));
			return false;
		}
		
		// Not Next to Self
		if (!selfAdj && !clan.getClaimSet().isEmpty())
		{
			UtilPlayer.message(caller, F.main("Clans", "You must claim next to your other Territory."));
			return false;
		}
		
		// Claim Timer
		if (_clansManager.getUnclaimMap().containsKey(chunk))
		{
			if (!UtilTime.elapsed(_clansManager.getUnclaimMap().get(chunk), _clansManager.getReclaimTime()))
			{
				UtilPlayer.message(caller, F.main("Clans", "This Territory cannot be claimed for " + F.time(UtilTime.convertString(_clansManager.getReclaimTime() - (System.currentTimeMillis() - _clansManager.getUnclaimMap().get(chunk)), 1, UtilTime.TimeUnit.FIT)) + "."));
				
				return false;
			}
			else
			{
				_clansManager.getUnclaimMap().remove(chunk);
			}
		}
		
		// Enemies in Land
		for (Player cur : UtilServer.getPlayers())
		{
			if (UtilMath.offset(cur, caller) < 16) if (playerEnemy(caller, cur))
			{
				UtilPlayer.message(caller, F.main("Clans", "You cannot claim while enemies are nearby."));
				return false;
			}
		}
		
		// Recharge
//		if (!Recharge.Instance.use(caller, "Territory Claim", 60000, true, false)) return false;
		
		// Event
		PlayerClaimTerritoryEvent event = new PlayerClaimTerritoryEvent(caller, caller.getLocation().getChunk(), clan);
		
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return false;
		}
		
		// Task
		_clansManager.getClanDataAccess().claim(clan.getName(), chunk, caller.getName(), false);
		
		// Inform
		UtilPlayer.message(caller, F.main("Clans", "You claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		clan.inform(F.name(caller.getName()) + " claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
		
		return true;
	}
	
	public boolean checkBox(Chunk chunk, int req)
	{
		int boxed = 0;
		
		// This is bad. I know. But the other way doesn't seem to work.
		ClaimLocation down = ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX() + 0, chunk.getZ() + 1));
		ClaimLocation up = ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ() - 1));
		ClaimLocation right = ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX() + 1, chunk.getZ()));
		ClaimLocation left = ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX() - 1, chunk.getZ()));
		
		ClanInfo downClan = getOwner(down);
		ClanInfo upClan = getOwner(up);
		ClanInfo leftClan = getOwner(left);
		ClanInfo rightClan = getOwner(right);
		
		if (downClan != null) boxed++;
		if (upClan != null) boxed++;
		if (leftClan != null) boxed++;
		if (rightClan != null) boxed++;
		
		return (boxed >= req);
	}
	
	public boolean unclaimSteal(Player caller, ClanInfo clientClan, ClanInfo ownerClan)
	{
		if (!_clansManager.canUnclaimChunk(clientClan, ownerClan))
		{
			return false;
		}
		
		// Change Inform
		UtilPlayer.message(caller, F.main("Clans", "You can no longer 'steal' territory. " + "You simply unclaim it and it can not be reclaimed by anyone for 30 mintes." + "This was done to improve gameplay. Enjoy!"));
		
		// Inform
		UtilServer.broadcast(F.main("Clans", F.elem(clientClan.getName()) + " unclaimed from " + F.elem(ownerClan.getName()) + " at " + F.elem(UtilWorld.locToStrClean(caller.getLocation())) + "."));
		
		// Unclaim
		_clansManager.getClanDataAccess().unclaim(ClaimLocation.of(caller.getLocation().getChunk()), caller.getName(), true);
		
		return true;
	}
	
	public void promote(Player caller, String other)
	{
		ClanInfo clan = getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		ClansPlayer self = clan.getMembers().get(caller.getUniqueId());
		
		if (self.getRole().ordinal() < ClanRole.ADMIN.ordinal())
		{
			UtilPlayer.message(caller, F.main("Clans", "Only Clan Admins and Leaders can promote others."));
			return;
		}
		
		ClansPlayer target = clan.getClansPlayerFromName(other);
		
		if (target == null) return;
		
		if (target.getPlayerName().equals(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot promote yourself."));
			return;
		}
		
		if (self.getRole().ordinal() <= target.getRole().ordinal())
		{
			UtilPlayer.message(caller, F.main("Clans", "You do not outrank " + F.name(other) + "."));
			return;
		}
		
		// Task
		String newRank = "?";
		if (target.getRole() == ClanRole.RECRUIT)
		{
			_clansManager.getClanDataAccess().role(clan, target.getUuid(), ClanRole.MEMBER);
			newRank = "Member";
		}
		else if (target.getRole() == ClanRole.MEMBER)
		{
			_clansManager.getClanDataAccess().role(clan, target.getUuid(), ClanRole.ADMIN);
			newRank = "Admin";
		}
		else if (target.getRole() == ClanRole.ADMIN)
		{
			_clansManager.getClanDataAccess().role(clan, target.getUuid(), ClanRole.LEADER);
			newRank = "Leader";
			
			// Give Leader
			_clansManager.getClanDataAccess().role(clan, caller.getUniqueId(), ClanRole.ADMIN);
		}
		
		// Inform
		clan.inform(F.name(caller.getName()) + " promoted " + F.name(other) + " to " + F.elem(newRank) + ".", null);
	}
	
	public void demote(Player caller, String other)
	{
		ClanInfo clan = getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		ClansPlayer self = clan.getMembers().get(caller.getUniqueId());
		
		if (self.getRole().ordinal() < ClanRole.ADMIN.ordinal())
		{
			UtilPlayer.message(caller, F.main("Clans", "Only Clan Admins and Leaders can demote others."));
			return;
		}
		
		ClansPlayer target = clan.getClansPlayerFromName(other);
		
		if (target == null) return;
		
		if (target.getPlayerName().equals(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot demote yourself."));
			return;
		}
		
		if (self.getRole().ordinal() <= target.getRole().ordinal())
		{
			UtilPlayer.message(caller, F.main("Clans", "You do not outrank " + F.name(other) + "."));
			return;
		}
		
		if (target.getRole() == ClanRole.RECRUIT)
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot demote " + F.name(other) + " any further."));
			return;
		}
		
		// Task
		String newRank = "?";
		if (target.getRole() == ClanRole.MEMBER)
		{
			_clansManager.getClanDataAccess().role(clan, target.getUuid(), ClanRole.RECRUIT);
			newRank = "Recruit";
		}
		else if (target.getRole() == ClanRole.ADMIN)
		{
			_clansManager.getClanDataAccess().role(clan, target.getUuid(), ClanRole.MEMBER);
			newRank = "Member";
		}
		
		// Inform
		clan.inform(F.main("Clans", F.name(caller.getName()) + " demoted " + F.name(other) + " to " + F.elem(newRank) + "."), null);
	}
	
	public void kick(final Player caller, final String other)
	{
		final ClanInfo clan = getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		ClansPlayer self = clan.getMembers().get(caller.getUniqueId());
		ClansPlayer clansPlayer = clan.getClansPlayerFromName(other);
		
		if (self == null || clansPlayer == null) return;
		
		if (self.getRole() != ClanRole.LEADER && self.getRole() != ClanRole.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can kick members."));
			return;
		}
		
		if (clansPlayer.getRole() == ClanRole.LEADER)
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan leaders cannot be kicked."));
			UtilPlayer.message(caller, F.main("Clans", "To disband a clan, use /c disband"));
			return;
		}
		
		if ((clansPlayer.getRole() == ClanRole.LEADER && self.getRole() == ClanRole.ADMIN) || (clansPlayer.getRole() == ClanRole.ADMIN && self.getRole() == ClanRole.ADMIN))
		{
			UtilPlayer.message(caller, F.main("Clans", "You do not outrank " + F.name(other) + "."));
			return;
		}
		
		final Player player = UtilPlayer.searchOnline(null, other, false);
		
		Callback<Boolean> callback = new Callback<Boolean>()
		{
			@Override
			public void run(Boolean data)
			{
				// Inform
				if (player != null) UtilPlayer.message(player, F.main("Clans", F.name(caller.getName()) + " kicked you from " + F.elem("Clan " + clan.getName()) + "."));
				UtilPlayer.message(caller, F.main("Clans", "You kicked " + F.name(other) + " from your Clan."));
				clan.inform(F.main("Clans", F.name(caller.getName()) + " kicked " + F.name(other) + " from your Clan."), caller.getName());
			}
		};
		
		// Task
		if (player != null)
			_clansManager.getClanDataAccess().leave(clan, player, callback);
		else
			_clansManager.getClanDataAccess().leave(clan, clansPlayer, callback);
	}
	
	public boolean unclaim(final Player caller, final Chunk c)
	{
		ClanInfo clan = getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return false;
		}
		
		ClaimLocation chunk = ClaimLocation.of(c);
		ClanInfo ownerClan = getOwner(caller.getLocation());
		
		// Try to Steal
		if (ownerClan != null && !ownerClan.equals(clan)) if (unclaimSteal(caller, clan, ownerClan)) return true;
		
		// Role
		if (clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER && clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can unclaim Territory."));
			return false;
		}
		
		// Not Claimed
		if (ownerClan == null || !ownerClan.equals(clan))
		{
			UtilPlayer.message(caller, F.main("Clans", "This Territory is not owned by you."));
			return false;
		}
		
		// Event
		PlayerUnClaimTerritoryEvent event = new PlayerUnClaimTerritoryEvent(caller, caller.getLocation().getChunk(), ownerClan);
		
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return false;
		}
		
		// Task
		_clansManager.getClanDataAccess().unclaim(chunk, caller.getName(), true);
		
		// Inform
		UtilPlayer.message(caller, F.main("Clans", "You unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		clan.inform(F.name(caller.getName()) + " unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
		
		return true;
	}
	
	public boolean unclaimAll(final Player caller)
	{
		ClanInfo clan = getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return false;
		}
		
		if (clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader can unclaim all Territory."));
			return false;
		}
		
		// Unclaim
		ArrayList<ClaimLocation> toUnclaim = new ArrayList<>();
		
		for (ClaimLocation chunk : clan.getClaimSet())
		{
			toUnclaim.add(chunk);
		}
		
		for (ClaimLocation chunk : toUnclaim)
		{
			_clansManager.getClanDataAccess().unclaim(chunk, caller.getName(), true);
		}
		
		// Inform
		UtilPlayer.message(caller, F.main("Clans", "You unclaimed all your Clans Territory."));
		clan.inform(F.name(caller.getName()) + " unclaimed all your Clans Territory.", caller.getName());
		
		return true;
	}

	public void invite(Player caller, ClanInfo clan, Player target)
	{
		if (clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER && clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can send invites."));
			return;
		}

		if (target.getName().equals(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot invite yourself."));
			return;
		}
		
		if (clan.getAllies() > clan.getAlliesMaxWithMemberCountOf(clan.getSize() + 1))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot invite more members until you remove some allies."));
			return;
		}
		
		if (clan.isInvited(target.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan has already invited " + F.elem(target.getName()) + "."));
			return;
		}

		// Inform
		clan.inform(F.name(caller.getName()) + " invited " + F.name(target.getName()) + " to join your Clan.", caller.getName());
		UtilPlayer.message(caller, F.main("Clans", "You invited " + F.name(target.getName()) + " to join your Clan."));
		UtilPlayer.message(target, F.main("Clans", F.name(caller.getName()) + " invited you to join " + F.elem("Clan " + clan.getName()) + "."));
		UtilPlayer.message(target, F.main("Clans", "Type " + F.elem("/c join " + clan.getName()) + " to accept!"));

		// Task
		_clansManager.getClanDataAccess().invite(clan, target.getName(), caller.getName());
	}
	
}
