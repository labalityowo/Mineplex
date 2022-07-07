package mineplex.game.clans.clans;

import java.util.LinkedList;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansUtility.ClanRelation;
import mineplex.game.clans.clans.event.PlayerEnterTerritoryEvent;
import mineplex.game.clans.clans.nether.NetherManager;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.ClanTerritory;

public class ClansDisplay extends MiniPlugin
{
	private ClansManager _clansManager;
	
	public ClansDisplay(JavaPlugin plugin, ClansManager clans)
	{
		super("Clans Display", plugin);
		
		_clansManager = clans;
	}
	
	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER) return;
		
		for (Player cur : UtilServer.getPlayers())
			Update(cur);
	}
	
	public void Update(Player player)
	{
		if (player.getWorld().getEnvironment() != Environment.NORMAL) return;
		
		ClientClan client = _clansManager.Get(player);
		if (client == null) return;
		
		// Same Chunk
		if (client.getTerritory().equals(UtilWorld.chunkToStr(player.getLocation().getChunk()))) return;
		
		// Update Territory
		client.setTerritory(UtilWorld.chunkToStr(player.getLocation().getChunk()));
		
		// AutoClaim
		if (client.isAutoClaim() && !(Managers.get(NetherManager.class).isInNether(player) || Managers.get(WorldEventManager.class).getRaidManager().isInRaid(player.getLocation()))) _clansManager.getClanAdmin().claim(player);
		
		// Map
		String owner = "Wilderness";
		ClanInfo ownerClan = _clansManager.getClanUtility().getOwner(player.getLocation());
		if (ownerClan != null) owner = ownerClan.getName();
		
		boolean safe = _clansManager.getClanUtility().isSafe(player);
		
		PlayerEnterTerritoryEvent event = new PlayerEnterTerritoryEvent(player, client.getOwner(), owner, safe, true);
		
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (!client.isMapOn())
		{
			boolean showChange = false;
			
			// Owner Change
			if (!client.getOwner().equals(owner))
			{
				client.setOwner(owner);
				showChange = true;
			}
			
			// Safe Change
			if (safe != client.isSafe())
			{
				client.setSafe(safe);
				showChange = true;
			}
			
			if (showChange)
			{
				if (event.willSendMessage()) displayOwner(player);
			}
		}
		else
		{
			if (event.willSendMessage()) displayOwner(player);
			
			displayMap(player);
		}
	}
	
	public void displayOwner(Player player)
	{
		// Name
		String ownerString = C.xWilderness + "Wilderness";
		
		ClanTerritory claim = _clansManager.getClanUtility().getClaim(player.getLocation());
		if (claim != null)
		{
			// Relation
			ClanRelation relation = _clansManager.getClanUtility().relPT(player, claim.ClaimLocation);
			
			// Name
			ownerString = _clansManager.getClanUtility().mRel(relation, claim.Owner, false);
			
			// Trust
			if (relation == ClanRelation.ALLY_TRUST) ownerString += " " + C.mBody + "(" + C.mElem + "Trusted" + C.mBody + ")";
		}
		
		if (_clansManager.getNetherManager().isInNether(player))
		{
			ownerString = C.cClansNether + "The Nether";
			if (_clansManager.getClanUtility().isSafe(player.getLocation()))
			{
				ownerString = C.cClansNether + "Nether Spawn";
			}
		}
		
		if (_clansManager.getWorldEvent().getRaidManager().isInRaid(player.getLocation()))
		{
			ownerString = C.cDRed + "Raid World";
		}
		
//		if (_clansManager.getNetherManager().isInNether(player))
//		{
//			_clansManager.message(player, "You are not allowed to claim territory in " + F.clansNether("The Nether") + ".");
//			
//			ownerString = C.cRed + "The Nether";
//		}
		
		UtilTextMiddle.display("", ownerString, 0, 25, 10, player);
		UtilPlayer.message(player, F.main("Territory", ownerString));
	}
	
	public int width = 8;
	public int height = 4;
	
	public void displayMap(Player player)
	{
		if (player.getWorld().getEnvironment().equals(Environment.NETHER)) return;
		
		// Get Local
		LinkedList<String> local = mLocalMap(player, player.getLocation().getChunk(), true);
		
		// Get Home
		LinkedList<String> home = null;
		
		if (player.getItemInHand().getType() == Material.MAP)
		{
			ClanInfo clan = _clansManager.getClanUtility().getClanByPlayer(player);
			if (clan != null) if (clan.getHome() != null) home = mLocalMap(player, clan.getHome().getChunk(), false);
		}
		
		// Display
		if (home == null || local.size() != home.size())
			UtilPlayer.message(player, local);
			
		else
			for (int i = 0; i < local.size(); i++)
				UtilPlayer.message(player, local.get(i) + "            " + home.get(i));
				
		// Display legend for map symbols
		UtilPlayer.message(player, C.xNone + "X: Current, #: Claimed, H: Home");
		UtilPlayer.message(player, C.xNone + "S: Safe, +: Admin, -: Unclaimed");
	}
	
	public LinkedList<String> mLocalMap(Player player, Chunk chunk, boolean local)
	{
		if (chunk == null) return null;
		
		LinkedList<String> localMap = new LinkedList<String>();
		
		for (int i = (chunk.getX() - height); i <= (chunk.getX() + height); i++)
		{
			String output = C.xNone + "<";
			
			for (int j = (chunk.getZ() + width); j >= (chunk.getZ() - width); j--)
			{
				Chunk curChunk = player.getWorld().getChunkAt(i, j);
				
				// Count Players
				int pCount = 0;
				if (player.getItemInHand().getType() == Material.MAP)
				{
					for (Player cur : UtilServer.getPlayers())
						if (cur.getLocation().getChunk().toString().equals(curChunk.toString())) pCount++;
				}
				
				// Get Data
				ClanInfo curOwner = _clansManager.getClanUtility().getOwner(ClaimLocation.of(curChunk));
				ClanTerritory curClaim = _clansManager.getClanUtility().getClaim(ClaimLocation.of(curChunk));
				
				// Add Icon
				if (i == chunk.getX() && j == chunk.getZ())
					output += getMapIcon(_clansManager.getClanUtility().relPC(player, curOwner), curClaim, curOwner, curChunk, pCount, true, local);
				else
					output += getMapIcon(_clansManager.getClanUtility().relPC(player, curOwner), curClaim, curOwner, curChunk, pCount, false, local);
			}
			
			output += ">";
			
			// Send
			localMap.add(output);
		}
		
		return localMap;
	}
	
	public String getMapIcon(ClanRelation relation, ClanTerritory claim, ClanInfo owner, Chunk chunk, int players, boolean mid, boolean local)
	{
		if (players > 9) players = 9;
		
		if (mid && local)
		{
			if (players > 0)
				return "" + C.cWhite + players;
			else
				return "" + C.cWhite + "X";
		}
		
		if (owner == null || claim == null)
		{
			if (players > 0)
				return "" + C.xNone + players;
			else
				return "" + C.xNone + "-";
		}
		
		if (claim.Safe)
		{
			if (players > 0)
				return "" + C.xSafe + players;
			else
				return "" + C.xSafe + "S";
		}
		
		if (owner.isAdmin())
		{
			if (players > 0)
				return "" + C.xAdmin + players;
			else
				return "" + C.xAdmin + "+";
		}
		
		if (relation == ClanRelation.SELF)
		{
			if (players > 0)
				return "" + C.xSelf + players;
			else if (_clansManager.getClanUtility().isChunkHome(owner, chunk))
				return "" + C.xSelf + "H";
			else
				return "" + C.xSelf + "#";
		}
		
		if (relation == ClanRelation.ALLY)
		{
			if (players > 0)
				return "" + C.xAlly + players;
			else if (_clansManager.getClanUtility().isChunkHome(owner, chunk))
				return "" + C.xAlly + "H";
			else
				return "" + C.xAlly + "#";
		}
		
		if (relation == ClanRelation.ALLY_TRUST)
		{
			if (players > 0)
				return "" + C.xdAlly + players;
			else if (_clansManager.getClanUtility().isChunkHome(owner, chunk))
				return "" + C.xdAlly + "H";
			else
				return "" + C.xdAlly + "#";
		}
		
		if (players > 0)
			return "" + C.xEnemy + players;
		else if (_clansManager.getClanUtility().isChunkHome(owner, chunk))
			return "" + C.xEnemy + "H";
		else
			return "" + C.xEnemy + "#";
	}
}
