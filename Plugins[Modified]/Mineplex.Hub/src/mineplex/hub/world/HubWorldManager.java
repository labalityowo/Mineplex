package mineplex.hub.world;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.BrewerInventory;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;
import mineplex.hub.HubManager;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

/**
 * A World Manager class that handles all world related events.
 * All events are run on {@link EventPriority#LOWEST} as not to conflict with any overriding done by other components.
 */
@ReflectivelyCreateMiniPlugin
public class HubWorldManager extends MiniPlugin
{

	private static final int MAX_LIVE_TICKS = 20 * 30;

	private final HubManager _manager;
	private final BlockRestore _blockRestore;
	private final MineplexWorld _worldData;

	private HubWorldManager()
	{
		super("Hub World");

		_manager = require(HubManager.class);
		_blockRestore = require(BlockRestore.class);
		_worldData = _manager.getWorldData();

		World world = _worldData.getWorld();

		world.setGameRuleValue("showDeathMessages", "false");
		world.setGameRuleValue("doDayNightCycle", "false");
		world.setDifficulty(Difficulty.EASY);
	 }

	/**
	 * Prevent block breaking
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockBreak(BlockBreakEvent event)
	{
		event.setCancelled(shouldCancel(event.getPlayer()));
	}

	/**
	 * Prevent block placing
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockPlace(BlockPlaceEvent event)
	{
		event.setCancelled(shouldCancel(event.getPlayer()));
	}

	/**
	 * Prevent item drop
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void itemDrop(PlayerDropItemEvent event)
	{
		event.setCancelled(shouldCancel(event));
	}

	/**
	 * Prevent item pickup
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void itemPickup(PlayerPickupItemEvent event)
	{
		event.setCancelled(shouldCancel(event));
	}

	/**
	 * Prevent block burning
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent blocks catching fire
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockIgnite(BlockIgniteEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent falling blocks becoming solid
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockEntityChange(EntityChangeBlockEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevents liquid flow
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockPhysics(BlockPhysicsEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevents block growth
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockGrow(BlockGrowEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevents trees growing
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void structureGrow(StructureGrowEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent entities catching fire
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void entityCombust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent armour stand manipulation
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void armourStand(PlayerArmorStandManipulateEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent entities taking damage
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void powerupDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof EnderCrystal)
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Prevent entities taking damage
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void entityDamage(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.VOID)
		{
			Entity entity = event.GetDamageeEntity();

			if (entity instanceof Player)
			{
				entity.eject();
				entity.leaveVehicle();
				entity.teleport(_manager.GetSpawn());
			}
			else if (!UtilEnt.hasFlag(entity, UtilEnt.FLAG_NO_REMOVE))
			{
				entity.remove();
			}
		}

		event.SetCancelled("Hub World");
	}

	/**
	 * Prevent creeper explosions
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void explosion(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}

	/**
	 * Prevent block spreading, e.g vines
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockSpread(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent leaves decaying
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void leavesDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent block fading
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent block forming, e.g ice
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockForm(BlockFormEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent inventory interation
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void inventoryClick(InventoryClickEvent event)
	{
		event.setCancelled(shouldCancel(event.getWhoClicked()));
	}

	/**
	 * Prevent hunger loss
	 */
	@EventHandler
	public void playerFood(FoodLevelChangeEvent event)
	{
		event.setFoodLevel(20);
	}

	/**
	 * Prevents emptying buckets
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevents filling buckets
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerBucketFill(PlayerBucketFillEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent players from leaving the world border
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void outOfBounds(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		World world = _manager.GetSpawn().getWorld();

		for (Player player : world.getPlayers())
		{
			if (!UtilAlg.inBoundingBox(player.getLocation(), _worldData.getMin(), _worldData.getMax()))
			{
				player.eject();
				player.leaveVehicle();
				player.teleport(_manager.GetSpawn());
			}
		}
	}

	/**
	 * Removes all items and projectiles that have been alive for over 30 seconds.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void oldEntityDespawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Entity entity : _manager.GetSpawn().getWorld().getEntities())
		{
			if (!(entity instanceof Item || entity instanceof Projectile) || entity.getTicksLived() < MAX_LIVE_TICKS)
			{
				continue;
			}

			entity.remove();
		}
	}

	/**
	 * Prevents rain/storms in the hub.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void weatherChange(WeatherChangeEvent event)
	{
		if (event.toWeatherState())
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Prevent eggs from spawning
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void eggSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity() instanceof Egg)
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Prevent the crafting of items
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void itemCraft(CraftItemEvent event)
	{
		event.setCancelled(true);
	}

	/**
	 * Prevent chunks unloading on players or chest podiums
	 */
	@EventHandler
	public void chunkUnload(ChunkUnloadEvent event)
	{
		for (Entity entity : event.getChunk().getEntities())
		{
			if (entity instanceof Player)
			{
				event.setCancelled(true);
				return;
			}
		}

		for (Block block : _blockRestore.getBlocks().keySet())
		{
			if (block.getChunk().equals(event.getChunk()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	/**
	 * Prevent opening beacons
	 */
	@EventHandler
	public void openBeacon(InventoryOpenEvent event)
	{
		if (event.getInventory() instanceof BeaconInventory || event.getInventory() instanceof BrewerInventory)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entitySpawn(EntitySpawnEvent event)
	{
		if (event.getEntity() instanceof Boat || event.getEntity() instanceof ThrownPotion)
		{
			event.setCancelled(true);
		}
	}

	/**
	 * This event only works for {@link PlayerEvent} and {@link EntityEvent} events.
	 *
	 * @return false if the entity within the event is a player and in creative mode.
	 */
	private boolean shouldCancel(Cancellable event)
	{
		if (event instanceof PlayerEvent)
		{
			return shouldCancel(((PlayerEvent) event).getPlayer());
		}
		else if (event instanceof EntityEvent)
		{
			EntityEvent entityEvent = (EntityEvent) event;

			if (entityEvent.getEntity() instanceof Player)
			{
				return shouldCancel((Player) entityEvent.getEntity());
			}
		}

		return true;
	}

	private boolean shouldCancel(HumanEntity player)
	{
		return player.getGameMode() != GameMode.CREATIVE;
	}
}
