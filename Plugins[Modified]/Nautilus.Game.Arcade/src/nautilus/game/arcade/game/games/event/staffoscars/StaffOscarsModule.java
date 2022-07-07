package nautilus.game.arcade.game.games.event.staffoscars;

import mineplex.core.common.util.*;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.modules.Module;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StaffOscarsModule extends Module
{

	private static final int MAX_X = 18;

	private List<Location> _blocks;
	private boolean _open;
	private boolean _animate;
	private int _x;
	private Location _center;

	private Set<ChairData> _chairs;

	private List<Location> _fireworks;

	private RainbowSheep _sheep;

	@Override
	protected void setup()
	{
		_chairs = new HashSet<>();
		_blocks = getGame().WorldData.GetCustomLocs(String.valueOf(Material.EMERALD_BLOCK.getId()));
		_center = UtilAlg.getAverageLocation(_blocks);
		_fireworks = getGame().WorldData.GetDataLocs("YELLOW");
		_sheep = new RainbowSheep();
		UtilServer.RegisterEvents(_sheep);
	}

	@Override
	public void cleanup()
	{
		UtilServer.Unregister(_sheep);
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Prepare)
		{
			return;
		}

		for (Location location : _blocks)
		{
			MapUtil.QuickChangeBlockAt(location, Material.WOOL, (byte) 14);
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_animate)
		{
			return;
		}

		for (Location location : _blocks)
		{
			boolean in = UtilMath.offset2d(location, _center) < _x;

			if (in && _open)
			{
				MapUtil.QuickChangeBlockAt(location, Material.AIR);
			}
			else if (!in && !_open)
			{
				MapUtil.QuickChangeBlockAt(location, Material.WOOL, (byte) 14);
			}
		}

		if (_open)
		{
			_x++;
		}
		else
		{
			_x--;
		}

		if (_open && _x == MAX_X || !_open && _x == -1)
		{
			_animate = false;
		}
	}

	@EventHandler
	public void curtainCommand(PlayerCommandPreprocessEvent event)
	{
		String message = event.getMessage();

		if (message.startsWith("/curtain") && getGame().getArcadeManager().GetGameHostManager().isAdmin(event.getPlayer(), false))
		{
			_open = !_open;
			_animate = true;

			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Event", "Curtain open state = " + _open + "."));
		}
	}

	@EventHandler
	public void chairInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block == null)
		{
			return;
		}

		if (UtilBlock.usable(block))
		{
			event.setCancelled(true);
		}

		if (block.getType() != Material.WOOD_STAIRS)
		{
			return;
		}

		for (ChairData data : _chairs)
		{
			if (data.getBlock().equals(block))
			{
				return;
			}

			if (data.getPlayer().equals(player))
			{
				handleDismount(player);
				break;
			}
		}

		_chairs.add(new ChairData(player, block));
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		handleDismount(event.getPlayer());
	}

	@EventHandler
	public void updateDismount(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Iterator<ChairData> iterator = _chairs.iterator();

		while (iterator.hasNext())
		{
			ChairData data = iterator.next();

			if (data.getPlayer().isInsideVehicle())
			{
				continue;
			}

			data.getStand().remove();
			iterator.remove();
		}
	}

	private void handleDismount(Player player)
	{
		Iterator<ChairData> iterator = _chairs.iterator();

		while (iterator.hasNext())
		{
			ChairData data = iterator.next();

			if (!data.getPlayer().equals(player))
			{
				continue;
			}

			data.getStand().remove();
			iterator.remove();
		}
	}

	@EventHandler
	public void armourStandEdit(PlayerArmorStandManipulateEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void fireworksCommand(PlayerCommandPreprocessEvent event)
	{
		String message = event.getMessage();

		if (message.startsWith("/firework") && getGame().getArcadeManager().GetGameHostManager().isAdmin(event.getPlayer(), false))
		{
			event.setCancelled(true);

			FireworkEffect fireworkEffect = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.YELLOW).build();

			for (Location location : _fireworks)
			{
				for (int i = 0; i < 4; i++)
				{
					UtilFirework.launchFirework(location, fireworkEffect, null, UtilMath.r(3) + 1);
				}
			}
		}
	}

	@EventHandler
	public void rainbowSheepCommand(PlayerCommandPreprocessEvent event)
	{
		String message = event.getMessage();

		if (!message.startsWith("/deantm") || !getGame().getArcadeManager().GetGameHostManager().isAdmin(event.getPlayer(), false))
		{
			return;
		}

		if (_sheep.isActive())
		{
			return;
		}

		event.setCancelled(true);
		_sheep.setActive(event.getPlayer());
	}
}
