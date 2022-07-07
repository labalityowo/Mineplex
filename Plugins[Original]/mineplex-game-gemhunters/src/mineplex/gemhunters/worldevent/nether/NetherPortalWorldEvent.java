package mineplex.gemhunters.worldevent.nether;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.utils.UtilVariant;
import mineplex.gemhunters.loot.rewards.LootChestReward;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventState;
import mineplex.gemhunters.worldevent.WorldEventType;

public class NetherPortalWorldEvent extends WorldEvent
{

	private static final int PORTALS = 5;
	private static final long MAX_TIME = TimeUnit.MINUTES.toMillis(20);
	private static final int SKELETONS_PER_PORTAL = 10;
	private static final long CASH_OUT_DELAY = TimeUnit.MINUTES.toMillis(10);
	
	private final BlockRestore _restore;
	
	private Player _player;
	private List<Location> _portalLocations;
	private List<Block> _portalBlocks;
	private long _portalLit;
	
	public NetherPortalWorldEvent()
	{
		super(WorldEventType.NETHER);
		
		_restore = Managers.require(BlockRestore.class);
		_portalLocations = new ArrayList<>();
		_portalBlocks = new ArrayList<>(50);
		_portalLit = 0;
	}

	@EventHandler
	public void lightPortal(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}
		
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.FLINT_AND_STEEL)
		{
			return;
		}
		
		if (!_portalBlocks.contains(block))
		{
			return;
		}
				
		if (_portalLit == 0)
		{
			_portalLit = System.currentTimeMillis();
			
			for (Location location : _portalLocations)
			{
				lightPortal(location);
			}
			
			_player = player;
			UtilServer.broadcast(F.main(_worldEvent.getName(), F.name(player.getName()) + " has lit a nether portal and become the " + F.color("Pumpkin King", C.cGold) + "! They now have 5 Omega Chests!"));
			ItemStack itemStack = new ItemBuilder(Material.ENDER_CHEST).setTitle(C.cAqua + "5 Omega Chests").build();
			LootChestReward reward = new LootChestReward(CASH_OUT_DELAY, itemStack, "Omega", 5);
			
			_loot.addItemReward(reward);
			
			if (UtilInv.hasSpace(player, 1))
			{
				reward.collectItem(player);
				player.getInventory().addItem(itemStack);
			}
			else
			{
				_worldData.World.dropItemNaturally(player.getLocation(), itemStack);
			}
						
			for (Location location : _portalLocations)
			{
				for (int i = 0; i < SKELETONS_PER_PORTAL; i++)
				{
					Skeleton skeleton = UtilVariant.spawnWitherSkeleton(location);
					
					skeleton.setCustomName(C.cGold + "Pumpkin Minions");
					skeleton.setCustomNameVisible(true);
					
					addEntity(skeleton);
				}
			}
			
			setEventState(WorldEventState.LIVE);
		}
	}
	
	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		if (_player != null && event.getPlayer().equals(_player))
		{
			setEventState(WorldEventState.COMPLETE);
		}
	}
	
	@EventHandler
	public void entityTarget(EntityTargetEvent event)
	{
		if (_player != null && _entities.contains(event) && _player.equals(event.getTarget()))
		{
			event.setCancelled(true);
		}
	}
		
	@Override
	public void onStart()
	{
		List<Location> storedLocations = _worldData.getCustomLocation("NETHER_PORTAL");
			
		for (int i = 0; i < PORTALS; i++)
		{
			Location location = UtilAlg.Random(storedLocations);
			
			if (_portalLocations.contains(location))
			{
				continue;
			}
			
			_portalLocations.add(location);
			buildPortal(location);
		}
		
		UtilServer.broadcast(F.main(_worldEvent.getName(), "Portals have spawned around the map!"));
	}

	@Override
	public boolean checkToEnd()
	{	
		return UtilTime.elapsed(_start, MAX_TIME);
	}

	@Override
	public void onEnd()
	{
		_portalLocations.clear();
	
		for (Block block : _portalBlocks)
		{
			_restore.restore(block);
		}
	
		_portalLit = 0;
	}

	@Override
	public Location[] getEventLocations()
	{
		return _portalLocations.toArray(new Location[0]);
	}
	
	@Override
	public double getProgress()
	{
		return (double) (_start + MAX_TIME - System.currentTimeMillis()) / (double) MAX_TIME;
	}
	
	private void buildPortal(Location location)
	{
		location.getBlock().setType(Material.SPONGE);
		
		for (Block block : UtilBlock.getInBoundingBox(location.clone().add(2, 4, 0), location.clone().add(-2, 0, 0), false, true, false, false))
		{
			_restore.add(block, Material.OBSIDIAN.getId(), (byte) 0, Integer.MAX_VALUE);
			_portalBlocks.add(block);
		}
	}
	
	private void lightPortal(Location location)
	{
		for (Block block : UtilBlock.getInBoundingBox(location.clone().add(1, 3, 0), location.clone().add(-1, 1, 0), false, true, false, false))
		{
			_restore.add(block, Material.PORTAL.getId(), (byte) 0, Integer.MAX_VALUE);
			_portalBlocks.add(block);
		}
	}

}
