package mineplex.game.clans.clans.siege.outpost;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import com.google.common.collect.Lists;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.PlayerClaimTerritoryEvent;
import mineplex.game.clans.clans.siege.SiegeManager;
import mineplex.game.clans.clans.siege.repository.OutpostRepository;
import mineplex.game.clans.clans.siege.repository.tokens.OutpostToken;
import mineplex.game.clans.core.repository.ClanTerritory;

public class OutpostManager extends MiniPlugin
{
	private ClansManager _clansManager;
	
	private NautHashMap<String, Outpost> _outposts = new NautHashMap<>();
	private NautHashMap<Integer, Outpost> _idToOutpost = new NautHashMap<>();
	
	private List<String> _removalQueue;
	
	private SiegeManager _siegeManager;
	
	private OutpostRepository _repository;
	
	public OutpostManager(ClansManager clansManager, SiegeManager siegeManager)
	{
		super("Outpost Manager", clansManager.getPlugin());
		
		_siegeManager = siegeManager;
		
		_clansManager = clansManager;
		
		_repository = new OutpostRepository(clansManager.getPlugin(), this);
		
		_removalQueue = new ArrayList<>();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlaceBlock(BlockPlaceEvent event)
	{
		if (!Clans.HARDCORE)
		{
			return;
		}
		if (event.getItemInHand().isSimilar(Outpost.OUTPOST_ITEM))
			if (!spawnOutpost(event.getPlayer(), event.getBlock().getLocation(), OutpostType.MK_III))
				event.setCancelled(true);
	}
	
	public boolean spawnOutpost(Player player, Location location, OutpostType type)
	{
		if (_clansManager.getNetherManager().isInNether(player))
		{
			_clansManager.message(player, "You are not allowed to place this in " + F.clansNether("The Nether") + ".");
			
			return false;
		}
		
		if (_clansManager.getWorldEvent().getRaidManager().isInRaid(player.getLocation()))
		{
			_clansManager.message(player, "You are not allowed to place this in a raid.");
			
			return false;
		}
		
		if (!_clansManager.isInClan(player))
		{
			UtilPlayer.message(player, F.main("Clans", "You must be in a Clan to place an Outpost."));
			return false;
		}

		if (_clansManager.hasTimer(player))
		{
			UtilPlayer.message(player, F.main("Clans", "You can't place an Outpost whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			return false;
		}
		
		if (location.getBlockY() < 30)
		{
			UtilPlayer.message(player, F.main("Clans", "You cannot place an Outpost this deep."));
			return false;
		}

		ClanInfo clan = _clansManager.getClan(player);
		
		if (UtilItem.isBoundless(location.clone().subtract(0, 1, 0).getBlock().getType()))
		{
			UtilPlayer.message(player, F.main("Clans", "An Outpost must not be placed floating."));
			return false;
		}
		
		if (Get(clan) != null)
		{
			UtilPlayer.message(player, F.main("Clans", "Your Clan already has an outpost."));
			return false;
		}
		
		if (_clansManager.getClanUtility().getClaim(location) != null)
		{
			UtilPlayer.message(player, F.main("Clans", "An Outpost must be placed in the Wilderness."));
			return false;
		}
		
		for (Outpost outpost : _outposts.values())
		{
			if (UtilMath.offset(location, outpost.getExactMiddle()) < type._size + 8)
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot place an Outpost near other Outposts."));
				return false;
			}
		}
		
		for (int x = -2; x < 2; x++)
		{
			for (int z = -2; z < 2; z++)
			{
				Chunk chunk = location.getWorld().getChunkAt(location.getChunk().getX() + x, location.getChunk().getZ() + z);
				
				ClanTerritory claim = _clansManager.getClanUtility().getClaim(chunk);
				
				if (claim != null)
				{
					if (!claim.Owner.equals(clan.getName()) && _clansManager.getBlacklist().allowed(claim.Owner))
					{
						UtilPlayer.message(player, F.main("Clans", "You cannot place an Outpost this close to a Clan's Territory."));
						return false;
					}
				}
			}
		}
		
		boolean gut = false;
		
		for (int x = -6; x < 6; x++)
		{
			for (int z = -6; z < 6; z++)
			{
				Chunk chunk = location.getWorld().getChunkAt(location.getChunk().getX() + x, location.getChunk().getZ() + z);
				
				ClanTerritory claim = _clansManager.getClanUtility().getClaim(chunk);
				
				if (claim != null)
				{
					System.out.println(claim.Owner + ", " + clan.getName());
					
					if (!claim.Owner.equals(clan.getName()) && _clansManager.getBlacklist().allowed(claim.Owner))
					{
						gut = true; /* das ist gut!!! */
						break;
					}
				}
			}
		}
		
		/* das ist schlecht */
		if (!gut)
		{
			UtilPlayer.message(player, F.main("Clans", "An Outpost must be placed near a rival Clan's Territory."));
			return false;
		}
		
		for (int x = -type._size; x < type._size; x++)
		{
			for (int y = -1; y < type._ySize; y++)
			{
				for (int z = -type._size; z < type._size; z++)
				{
					Location loc = location.clone().add(x, y, z);
					
					if (_clansManager.getClanUtility().isClaimed(loc))
					{
						UtilPlayer.message(player, F.main("Clans", "You cannot place an Outpost where it may intersect with claimed territory."));
						return false;
					}
				}
			}
		}
		
		_outposts.put(clan.getName(), new Outpost(this, clan, location, type));
		_idToOutpost.put(_outposts.get(clan.getName()).getUniqueId(), _outposts.get(clan.getName()));
		
		return true;
	}
	
	@EventHandler
	public void onBlockFall(EntityChangeBlockEvent event)
	{
		if (event.getEntity().hasMetadata("ClansOutpost"))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onClaim(PlayerClaimTerritoryEvent event)
	{
		for (Outpost outpost : _outposts.values())
		{
			if (outpost.getBounds().b(UtilAlg.toBoundingBox(event.getClaimedChunk().getBlock(0, 0, 0).getLocation(), event.getClaimedChunk().getBlock(15, 254, 15).getLocation())))
			{
				event.setCancelled(true);
				UtilPlayer.message(event.getClaimer(), F.main("Clans", "You cannot claim this territory as it overlaps with " + F.elem(outpost.getOwner().getName()) + "'s Outpost."));
				break;
			}
		}
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
			_outposts.values()
				.stream()
				.forEach(Outpost::update);
		
		if (event.getType() == UpdateType.FASTER)
			if (!_removalQueue.isEmpty())
				HandlerList.unregisterAll(_idToOutpost.remove(_outposts.remove(_removalQueue.remove(0)).getUniqueId()));
		
		if (event.getType() == UpdateType.TWOSEC)
			_outposts.values()
						.stream()
							.filter(outpost -> outpost.getState() == OutpostState.LIVE && outpost.getLifetime() > Outpost.MAX_LIFETIME)
						.forEach(Outpost::kill);
	}
	
	public Outpost Get(ClanInfo clan)
	{
		return clan == null ? null : _outposts.get(clan.getName());
	}

	public void queueForRemoval(String name)
	{
		_removalQueue.add(name);
	}

	public SiegeManager getSiegeManager()
	{
		return _siegeManager;
	}

	public List<Outpost> getOutposts()
	{
		return Lists.newArrayList(_outposts.values());
	}
	
	public Outpost Get(int outpostId)
	{
		return _idToOutpost.get(outpostId);
	}

	public ClansManager getClansManager()
	{
		return _clansManager;
	}
	
	public void loadOutposts()
	{
		System.out.println("[OUTPOSTS] LOADING OUTPOSTS FROM DATABASE");
		
		_repository.getOutpostsByServer(_clansManager.getServerId(), tokens ->
			tokens.forEach(token ->
			{
				Outpost outpost = new Outpost(this, token);
				
				if ((System.currentTimeMillis() - token.TimeSpawned) > Outpost.MAX_LIFETIME)
				{
					System.out.println("[OUTPOSTS] SKIPPING & REMOVING OUTPOST [" + token.UniqueId + "] BECAUSE OF OLD AGE");
					
					_repository.deleteOutpost(token.UniqueId);
					outpost.kill();
					
					return;
				}
				
				System.out.println("[OUTPOSTS] INITIALIZED OUTPOST FROM DATABASE SAVE");
				
				_outposts.put(token.OwnerClan.getName(), outpost);
			})
		);
	}
	
	public void saveOutposts()
	{
		final Stack<Runnable> queue = new Stack<>();
		
		_outposts.values().forEach(outpost ->
		{
			final OutpostToken token = outpost.toToken();
			
			queue.push(() -> _repository.updateOutpost(token));
		});
		
		runAsync(() ->
		{
			while (!queue.isEmpty())
				queue.pop().run();
			
			_repository.getOutpostsByServer(_clansManager.getServerId(), tokens ->
			{
				tokens.forEach(token ->
				{
					if (!_idToOutpost.containsKey(token.UniqueId))
					{
						System.out.println("[OUTPOSTS] OUTPOST [" + token.UniqueId + "] NO LONGER EXISTS, DELETING");
						_repository.deleteOutpost(token.UniqueId);
					}
				});
			});
		});
	}

	public OutpostRepository getRepository()
	{
		return _repository;
	}
}