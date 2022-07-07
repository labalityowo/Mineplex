package mineplex.clanshub;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * Manager for the hub world
 */
public class WorldManager extends MiniPlugin
{
	public HubManager Manager;

	public WorldManager(HubManager manager)
	{
		super("World Manager", manager.getPlugin());

		Manager = manager;
		
		World world = UtilWorld.getWorld("world");
		
		world.setGameRuleValue("doDaylightCycle", "false");
									
		world.setTime(6000);
		world.setStorm(false);
		world.setThundering(false);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return; 

		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplosion(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}

	@EventHandler
	public void onVineGrow(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onLeafDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onBorderUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (UtilMath.offset(player.getLocation(), Manager.GetSpawn()) > 50)
			{
				player.eject();
				player.leaveVehicle();
				player.teleport(Manager.GetSpawn());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onItemPickup(PlayerPickupItemEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onItemDrop(PlayerDropItemEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemDespawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Entity ent : UtilWorld.getWorld("world").getEntities())
		{
			if (!(ent instanceof Item))
				continue;
			
			if (((Item)ent).getItemStack().getType() == Material.MONSTER_EGG)
				continue;
			
			if (UtilEnt.GetMetadata(ent, "UtilItemSpawning") != null)
				continue;
			
			if (ent.getTicksLived() > 1200)
				ent.remove();
		}
	}

	@EventHandler
	public void onWeather(WeatherChangeEvent event)
	{
		if (!event.getWorld().getName().equals("world"))
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockForm(BlockFormEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBoatBreak(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Boat)
		{
			event.SetCancelled("Boat Cancel");
		}
	}
	
	@EventHandler
	public void prevenCombustiont(EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void preventMobs(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() == SpawnReason.NATURAL || event.getSpawnReason() == SpawnReason.NETHER_PORTAL)
		{
			event.setCancelled(true);
		}
	}
}