package mineplex.game.nano.game.components.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.util.Vector;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.components.ComponentHook;
import mineplex.game.nano.game.event.GameStateChangeEvent;

@ReflectivelyCreateMiniPlugin
public class GameWorldManager extends GameManager implements ComponentHook<GameWorldComponent>
{

	private GameWorldComponent _hook;

	private GameWorldManager()
	{
		super("World Hook");
	}

	@Override
	public void setHook(GameWorldComponent hook)
	{
		_hook = hook;
	}

	@Override
	public GameWorldComponent getHook()
	{
		return _hook;
	}

	@EventHandler
	public void gameDeath(GameStateChangeEvent event)
	{
		if (event.getState() == GameState.Dead)
		{
			setHook(null);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void creatureSpawn(CreatureSpawnEvent event)
	{
		if (_hook == null || !_hook.isCreatureAllow() && !_hook.isCreatureAllowOverride())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void creatureSpawnChunk(CreatureSpawnEvent event)
	{
		event.getEntity().getLocation().getChunk();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockBreak(BlockBreakEvent event)
	{
		if (_hook == null || !_hook.getGame().isLive() || !_hook.isBlockBreak() || UtilPlayer.isSpectator(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (_hook == null || !_hook.getGame().isLive() ||  !_hook.isBlockPlace() || UtilPlayer.isSpectator(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void weatherChange(WeatherChangeEvent event)
	{
		if (event.toWeatherState() && (_hook == null || !_hook.isAllowWeather()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void updateBoundary(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		MineplexWorld mineplexWorld;
		Location fallbackLocation = null;

		// No hook, in Lobby
		if (_hook == null)
		{
			mineplexWorld = _manager.getLobbyManager().getMineplexWorld();
			fallbackLocation = _manager.getLobbyManager().getSpawn();
		}
		else
		{
			mineplexWorld = _hook.getGame().getMineplexWorld();

			if (mineplexWorld != null)
			{
				fallbackLocation = _hook.getGame().getSpectatorLocation();
			}
		}

		if (mineplexWorld == null || fallbackLocation == null)
		{
			return;
		}

		Location min = mineplexWorld.getMin(), max = mineplexWorld.getMax();

		for (Player player : UtilServer.getPlayersCollection())
		{
			Location location = player.getLocation();

			// Inside map
			if (UtilAlg.inBoundingBox(location, min, max))
			{
				continue;
			}

			// No hook or spectator
			if (_hook == null || UtilPlayer.isSpectator(player))
			{
				player.teleport(fallbackLocation);
			}
			else
			{
				double damage = 4;

				// Instant kill or in void
				if (_hook.isWorldBoundaryKill() || location.getY() < min.getY())
				{
					damage = 500;
				}
				else
				{
					Vector velocity = UtilAlg.getTrajectory2d(location, fallbackLocation);
					velocity.setY(1);

					UtilAction.velocity(player, velocity);
					player.playSound(location, Sound.NOTE_BASS, 1, 0.5F);
					player.sendMessage(F.main(_manager.getName(), C.cRed + "RETURN TO THE PLAYABLE AREA!"));
				}

				_manager.getDamageManager().NewDamageEvent(player, null, null, DamageCause.CUSTOM, damage, false, true, true, _hook.getGame().getGameType().getName(), "World Border");
			}
		}

	}

	@EventHandler
	public void chunkUnload(ChunkUnloadEvent event)
	{
		if (_hook != null)
		{
			MineplexWorld mineplexWorld = _hook.getGame().getMineplexWorld();

			if (mineplexWorld != null && mineplexWorld.getWorld().equals(event.getWorld()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || (_hook != null && _hook.isBlockInteract()))
		{
			return;
		}

		Block block = event.getClickedBlock();
		Material material = block.getType();

		if (material != Material.WOODEN_DOOR && material != Material.IRON_DOOR_BLOCK && UtilBlock.usable(block))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockGrow(BlockGrowEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockFromTo(BlockFromToEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockSpread(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockIgnite(BlockIgniteEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void leavesDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void structureGrow(StructureGrowEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockForm(BlockFormEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void armourStand(PlayerArmorStandManipulateEvent event)
	{
		event.setCancelled(true);
	}
}
