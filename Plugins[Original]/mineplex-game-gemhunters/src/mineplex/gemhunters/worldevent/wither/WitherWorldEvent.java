
package mineplex.gemhunters.worldevent.wither;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.utils.UtilVariant;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventState;
import mineplex.gemhunters.worldevent.WorldEventType;

public class WitherWorldEvent extends WorldEvent
{

	private static final long MAX_TIME = TimeUnit.MINUTES.toMillis(5);
	private static final int SKELETONS = 5;
	private static final ItemStack IN_HAND = new ItemStack(Material.STONE_SWORD);
	private static final int HEALTH = 40;
	private static final int RADIUS = 5;
	
	private Location[] _skulls;
	
	public WitherWorldEvent()
	{
		super(WorldEventType.WITHER);
			
		_worldEvent.runSyncLater(() -> buildAlter(), 20);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void trigger(BlockPlaceEvent event)
	{
		if (_skulls == null)
		{
			return;
		}
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		boolean start = true;
		
		// Check for allowed placement
		for (Location location : _skulls)
		{	
			if (block.getLocation().equals(location))
			{
				event.setCancelled(false);
				player.sendMessage(F.main(_worldEvent.getName(), "grrrrr...."));
				break;
			}
		}
		
		// Check for all skulls
		for (Location location : _skulls)
		{
			if (location.getBlock().getType() != Material.SKULL)
			{
				start = false;
			}
		}
		
		// Start Event
		if (start)
		{			
			setEventState(WorldEventState.WARMUP);
		}
	}
	
	@Override
	public void onStart()
	{
		Location location = _worldData.getCustomLocation("WITHER_ALTER").get(0).clone().add(0, 1, 0);
		
		for (Location skull : _skulls)
		{
			skull.getBlock().setType(Material.AIR);
		}
		
		Block chest = location.getBlock().getRelative(BlockFace.UP);
		
		chest.setType(Material.ENDER_CHEST);
		_loot.addSpawnedChest(chest.getLocation(), "PURPLE");
		
		for (int i = 0; i < SKELETONS; i++)
		{
			Skeleton skeleton = UtilVariant.spawnWitherSkeleton(UtilAlg.getRandomLocation(location, RADIUS, 0, RADIUS));
			
			skeleton.getEquipment().setItemInHand(IN_HAND);
			skeleton.setMaxHealth(HEALTH);
			skeleton.setHealth(HEALTH);
			
			addEntity(skeleton);
		}
		
		setEventState(WorldEventState.LIVE);
	}

	@Override
	public boolean checkToEnd()
	{
		return UtilTime.elapsed(_start, MAX_TIME) || _entities.isEmpty();
	}

	@Override
	public void onEnd()
	{
		for (Location location : _skulls)
		{
			location.getBlock().setType(Material.AIR);
		}
	}

	@Override
	public Location[] getEventLocations()
	{
		return new Location[] { _skulls[1] };
	}
	
	@Override
	public double getProgress()
	{
		return (double) (_start + MAX_TIME - System.currentTimeMillis()) / (double) MAX_TIME;
	}
	
	private void buildAlter()
	{
		Location point = _worldData.getCustomLocation("WITHER_ALTER").get(0).clone();
		
		point.getBlock().setType(Material.SOUL_SAND);
		point.add(0, 1, 0).getBlock().setType(Material.SOUL_SAND);
		point.add(0, 0, -1).getBlock().setType(Material.SOUL_SAND);
		point.add(0, 0, 2).getBlock().setType(Material.SOUL_SAND);
		
		_skulls = new Location[] {
			point.add(0, 1, 0),
			point.add(0, 0, -1),
			point.add(0, 0, -1)
		};
		
		for (Location location : _skulls)
		{
			location.getBlock().setType(Material.SPONGE);
		}
	}

}
