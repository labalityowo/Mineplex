package nautilus.game.pvp.modules.clans;

import java.util.LinkedList;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import nautilus.game.pvp.modules.clans.ClansUtility.ClanRelation;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClansDisplay 
{
	private Clans Clans;
	public ClansDisplay(Clans clans)
	{
		Clans = clans;
	}
	
	public void Update(Player player) 
	{
		if (player.getWorld().getEnvironment() != Environment.NORMAL)
			return;

		CoreClient client = Clans.Clients().Get(player);	
		if (client == null)		return;

		//Same Chunk
		if (client.Clan().GetTerritory().equals(UtilWorld.chunkToStr(player.getLocation().getChunk())))
			return;

		//Update Territory
		client.Clan().SetTerritory(UtilWorld.chunkToStr(player.getLocation().getChunk()));

		//AutoClaim
		if (client.Clan().IsAutoClaim())
			Clans.CAdmin().claim(player);
		
		//Map
		String owner = "?";
		ClansClan ownerClan = Clans.CUtil().getOwner(player.getLocation());
		if (ownerClan != null)
			owner = ownerClan.GetName();

		boolean safe = Clans.CUtil().isSafe(player);

		if (!client.Clan().IsMapOn())
		{
			boolean showChange = false;

			//Owner Change
			if (!client.Clan().GetOwner().equals(owner))
			{
				client.Clan().SetOwner(owner);
				showChange = true;
			}

			//Safe Change
			if (safe != client.Clan().IsSafe())
			{
				client.Clan().SetSafe(safe);
				showChange = true;
			}

			if (showChange)
				displayOwner(player);
		}		
		else
		{
			displayOwner(player);
			displayMap(player);	
		}
	}

	public void displayOwner(Player player)
	{
		//Name 
		String ownerString = C.xWilderness + "Wilderness";

		ClansTerritory claim = Clans.CUtil().getClaim(player.getLocation());
		String append = "";
		if (claim != null)	
		{
			//Relation
			ClanRelation relation = Clans.CUtil().relPT(player.getName(), claim.chunk);
			
			//Name
			ownerString = Clans.CUtil().mRel(relation, claim.getOwner(), false);

			//Trust
			if (relation == ClanRelation.ALLY_TRUST)
				append = C.mBody + "(" + C.mElem + "Trusted" + C.mBody + ")";
			
			//Dom
			if (relation == ClanRelation.ENEMY) 
			{
				ClansClan clientClan = Clans.CUtil().getClanByPlayer(player);

				if (clientClan != null)
				{
					ClansClan ownerClan = Clans.CUtil().getClanByClanName(claim.owner);
					if (ownerClan != null)
						append = clientClan.getDominanceString(ownerClan);
				}	
			}
		}

		UtilPlayer.message(player, F.main("Clans", ownerString + " " + append));
	}

	public int width = 8;
	public int height = 4;

	public void displayMap(Player player)
	{
		if (player.getWorld().getEnvironment().equals(Environment.NETHER))
			return;

		//Get Local
		LinkedList<String> local = mLocalMap(player, player.getLocation().getChunk(), true);

		//Get Home
		LinkedList<String> home = null;

		if (player.getItemInHand().getType() == Material.MAP)
		{
			ClansClan clan = Clans.CUtil().getClanByPlayer(player);
			if (clan != null)
				if (clan.GetHome() != null)
					home = mLocalMap(player, clan.GetHome().getChunk(), false);
		}

		//Display
		if (home == null || local.size() != home.size())
			UtilPlayer.message(player, local);

		else
			for (int i = 0 ; i < local.size() ; i++)
				UtilPlayer.message(player, local.get(i) + "            " + home.get(i));
	}

	public LinkedList<String> mLocalMap(Player player, Chunk chunk, boolean local)
	{
		if (chunk == null)
			return null;

		LinkedList<String> localMap = new LinkedList<String>();

		for (int i=(chunk.getX()-height) ; i <= (chunk.getX()+height) ; i++)
		{
			String output = C.xNone + "<";

			for (int j=(chunk.getZ()+width) ; j >= (chunk.getZ()-width) ; j--)
			{
				Chunk curChunk = player.getWorld().getChunkAt(i, j);

				//Count Players
				int pCount = 0;
				if (player.getItemInHand().getType() == Material.MAP)
				{
					for (Player cur : UtilServer.getPlayers())
						if (cur.getLocation().getChunk().toString().equals(curChunk.toString()))
							pCount++;
				}

				//Get Data
				ClansClan curOwner = Clans.CUtil().getOwner(UtilWorld.chunkToStr(curChunk));
				ClansTerritory curClaim = Clans.CUtil().getClaim(UtilWorld.chunkToStr(curChunk));

				//Add Icon
				if (i == chunk.getX() && j == chunk.getZ())
					output += getMapIcon(Clans.CUtil().relPC(player.getName(), curOwner), curClaim, curOwner, curChunk, pCount, true, local);
				else
					output += getMapIcon(Clans.CUtil().relPC(player.getName(), curOwner), curClaim, curOwner, curChunk, pCount, false, local);
			}

			output += ">";

			//Send
			localMap.add(output);
		}

		return localMap;
	}

	public String getMapIcon(ClanRelation relation, ClansTerritory claim, ClansClan owner, Chunk chunk, int players, boolean mid, boolean local)
	{
		if (players > 9)
			players = 9;

		if (mid && local)
		{
			if (players > 0)	return "" + C.cWhite + players;
			else				return "" + C.cWhite + "X";
		}

		if (owner == null || claim == null)			
		{
			if (players > 0)	return "" + C.xNone + players;
			else				return "" + C.xNone + "-";
		}

		if (claim.safe)
		{
			if (players > 0)	return "" + C.xSafe + players;
			else				return "" + C.xSafe + "S";
		}

		if (owner.IsAdmin())		
		{
			if (players > 0)	return "" + C.xAdmin + players;
			else				return "" + C.xAdmin + "+";
		}


		if (relation == ClanRelation.SELF)
		{
			if (players > 0)									return "" + C.xSelf + players;
			else if (Clans.CUtil().isChunkHome(owner, chunk))	return "" + C.xSelf + "H";
			else												return "" + C.xSelf + "#";
		}

		if (relation == ClanRelation.ALLY)
		{
			if (players > 0)									return "" + C.xAlly + players;
			else if (Clans.CUtil().isChunkHome(owner, chunk))	return "" + C.xAlly + "H";
			else												return "" + C.xAlly + "#";
		}

		if (relation == ClanRelation.ALLY_TRUST)
		{
			if (players > 0)									return "" + C.xdAlly + players;
			else if (Clans.CUtil().isChunkHome(owner, chunk))	return "" + C.xdAlly + "H";
			else												return "" + C.xdAlly + "#";
		}

		if (players > 0)										return "" + C.xEnemy + players;
		else if (Clans.CUtil().isChunkHome(owner, chunk))		return "" + C.xEnemy + "H";
		else													return "" + C.xEnemy + "#";
	}

	public void handleInteract(PlayerInteractEvent event) 
	{
		if (event.getPlayer().getItemInHand().getType() != Material.MAP)
			return;

		if (!Clans.Recharge().use(event.getPlayer(), "Clan Map", 500, false))
			return;
		
		displayOwner(event.getPlayer());
		displayMap(event.getPlayer());	
	}


}
