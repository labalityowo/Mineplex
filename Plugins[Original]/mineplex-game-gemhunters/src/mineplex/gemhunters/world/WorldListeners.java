package mineplex.gemhunters.world;

import java.io.File;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilWorld;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class WorldListeners implements Listener
{
	private final JavaPlugin _plugin;

	public WorldListeners(JavaPlugin plugin)
	{
		_plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void deletePlayerData(PlayerQuitEvent event)
	{
		_plugin.getServer().getScheduler().runTaskLater(_plugin, () ->
		{
			World world = event.getPlayer().getWorld();
			UUID uuid = event.getPlayer().getUniqueId();
			String path = world.getWorldFolder().getPath();
			new File(path + File.separator + "playerdata" + File.separator + uuid + ".dat").delete();
			new File(path + File.separator + "stats" + File.separator + uuid + ".json").delete();
		}, 10);
	}

	@EventHandler
	public void customDamage(CustomDamageEvent event)
	{
		event.SetDamageToLevel(false);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void blockBreak(BlockBreakEvent event)
	{
		if (shouldBlock(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (event.getBlockPlaced().getType() != Material.CAKE_BLOCK && event.getItemInHand() != null && event.getItemInHand().getType() != Material.FLINT_AND_STEEL && shouldBlock(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void armorStandEdit(PlayerArmorStandManipulateEvent event)
	{
		if (shouldBlock(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void entityDestory(PlayerInteractAtEntityEvent event)
	{
		if (shouldBlock(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void entityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof ArmorStand || event.getEntity() instanceof ItemFrame)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void paintings(HangingBreakEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void itemFrames(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof ItemFrame)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
		{
			event.setCancelled(true);
		}

		Block block = event.getClickedBlock();

		if (block == null)
		{
			return;
		}

		Material material = block.getType();

		if (material == Material.BEACON || material == Material.DISPENSER || material == Material.HOPPER || material == Material.BREWING_STAND || material == Material.DROPPER)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void chunkUnload(ChunkUnloadEvent event)
	{
		if (!UtilWorld.inWorldBorder(new Location(event.getWorld(), event.getChunk().getX(), 0, event.getChunk().getZ())))
		{
			return;
		}

		if (event.getChunk().getEntities().length == 0)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void fireSpread(BlockIgniteEvent event)
	{
		if (event.getCause() == IgniteCause.FLINT_AND_STEEL)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void fireSpread(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockDecay(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void leavesDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void hungerChange(FoodLevelChangeEvent event)
	{
		Player player = (Player) event.getEntity();

		// Some witchcraft from the arcade, seems to make hunger not ridiculous.
		player.setSaturation(3.8F);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void weather(WeatherChangeEvent event)
	{
		if (event.toWeatherState())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void bucketEmpty(PlayerBucketEmptyEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void fishingExpRemove(PlayerFishEvent event)
	{
		event.setExpToDrop(0);
	}

	@EventHandler
	public void smelt(BlockExpEvent event)
	{
		Material material = event.getBlock().getType();

		if (material == Material.FURNACE || material == Material.BURNING_FURNACE)
		{
			event.setExpToDrop(0);
		}
	}

	@EventHandler
	public void removeExp(EntityDeathEvent event)
	{
		event.setDroppedExp(0);
	}

	@EventHandler
	public void removeExp(PlayerFishEvent event)
	{
		event.setExpToDrop(0);
	}

	public boolean shouldBlock(Player player)
	{
		return player.getGameMode() != GameMode.CREATIVE;
	}
}