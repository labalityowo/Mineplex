package mineplex.mapparser.module.modules;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.BackupTask;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.Parse;
import mineplex.mapparser.TickEvent;
import mineplex.mapparser.module.Module;

/**
 *
 */
public class EventModule extends Module
{

	private List<World> _updated = Lists.newArrayList();

	public EventModule(MapParser plugin)
	{
		super("Events", plugin);
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		player.teleport(getPlugin().getSpawnLocation());

		displayHelp(player);
	}

	@EventHandler
	public void onTick(TickEvent event)
	{
		for (World world : getPlugin().getServer().getWorlds())
		{
			if (_updated.contains(world))
			{
				continue;
			}
			if (world.getName().toLowerCase().contains("halloween"))
			{
				world.setTime(16000);
			}
			else
			{
				world.setTime(8000);
			}
			world.setStorm(false);
			world.setGameRuleValue("doDaylightCycle", "false");
			_updated.add(world);
		}
	}

	@EventHandler
	public void onParseUpdate(TickEvent event)
	{
		if (getPlugin().getCurParse() == null)
		{
			return;
		}

		Parse parse = getPlugin().getCurParse();

		if (parse.Update())
		{
			getPlugin().announce("Parse Completed!");

			getPlugin().announce("Cleaning and Creating ZIP...");

			try
			{
				getPlugin().getWorldManager().finalizeParsedWorld(parse.getWorld());
			} catch (Exception e)
			{
				getPlugin().announce("Creating ZIP Failed! Please Try Again!");
				e.printStackTrace();
			}

			getPlugin().setCurrentParse(null);
		}
	}

	@EventHandler
	public void SaveUnloadWorlds(TickEvent event)
	{
		for (World world : getPlugin().getServer().getWorlds())
		{
			if (world.getName().equalsIgnoreCase("world_lobby"))
			{
				continue;
			}

			if (world.getName().startsWith("parse_"))
			{
				continue;
			}

			if (!world.getName().startsWith("map"))
			{
				continue;
			}

			if (world.getPlayers().isEmpty())
			{
				getPlugin().announce("Saving & Closing World: " + F.elem(world.getName()));
				getPlugin().getServer().unloadWorld(world, true);
				_updated.remove(world);
				getPlugin()._mapsBeingZipped.add(world.getName());
				System.out.println("Starting backup of " + world);
				new BackupTask(getPlugin(), world.getName(), data ->
				{
					System.out.println("Finished backup of " + world);
					getPlugin()._mapsBeingZipped.remove(world.getName());
				});
			}
		}
	}

	@EventHandler
	public void Chat(AsyncPlayerChatEvent event)
	{
		event.setCancelled(true);

		String world = C.cDGreen + C.Bold + getPlugin().getShortWorldName(event.getPlayer().getWorld().getName());

		String name = C.cYellow + event.getPlayer().getName();
		if (getPlugin().getData(event.getPlayer().getWorld().getName()).HasAccess(event.getPlayer()))
		{
			name = C.cGreen + event.getPlayer().getName();
		}

		String grayName = C.cBlue + event.getPlayer().getName();
		String grayWorld = C.cBlue + C.Bold + event.getPlayer().getWorld().getName();

		for (Player player : getPlugin().getServer().getOnlinePlayers())
		{
			if (player.getWorld().equals(event.getPlayer().getWorld()))
			{
				player.sendMessage(world + ChatColor.RESET + " " + name + ChatColor.RESET + " " + event.getMessage());
			} else
			{
				player.sendMessage(grayWorld + ChatColor.RESET + " " + grayName + ChatColor.RESET + " " + C.cGray + event.getMessage());
			}

		}

		System.out.println(world + ChatColor.RESET + " " + name + ChatColor.RESET + " " + event.getMessage());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void InteractCancel(PlayerInteractEvent event)
	{
		if (event.getPlayer().isOp())
		{
			return;
		}
		//Permission
		if (!getPlugin().getData(event.getPlayer().getWorld().getName()).HasAccess(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	//#################################################################################################
	//#                                                                                               #
	//#                                     Simple methods                                            #
	//#                                                                                               #
	//#                                                                                               #
	//#################################################################################################

	@EventHandler
	public void Join(PlayerJoinEvent event)
	{
		event.setJoinMessage(F.sys("Player Join", event.getPlayer().getName()));

		event.getPlayer().setGameMode(GameMode.CREATIVE);
		event.getPlayer().setFlying(true);
	}

	@EventHandler
	public void Join(PlayerQuitEvent event)
	{
		event.setQuitMessage(F.sys("Player Quit", event.getPlayer().getName()));
	}

	@EventHandler
	public void onDroppedItemSpawn(EntitySpawnEvent event)
	{
		if (event.getEntityType() != EntityType.ARMOR_STAND)
		{
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void DisableBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void DisableIgnite(BlockIgniteEvent event)
	{
		if (event.getCause() == IgniteCause.LAVA || event.getCause() == IgniteCause.SPREAD)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void DisableFire(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void DisableFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void DisableDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void DisableIceForm(BlockFormEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void DisableWeather(WeatherChangeEvent event)
	{
		if (!_updated.contains(event.getWorld()))
		{
			return;
		}
		
		event.setCancelled(true);
	}
}
