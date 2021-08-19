package nautilus.game.arcade.game.games.bossbattles.displays;

import nautilus.game.arcade.game.games.bossbattles.BattleBoss;
import nautilus.game.arcade.game.games.bossbattles.BossBattles;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.hologram.Hologram;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public abstract class BossDisplay implements Listener
{
	private Location _bossLocation;
	protected BossBattles Plugin;
	private BattleBoss _boss;
	private Hologram _hologram;
	private ArrayList<Entity> _entities = new ArrayList<Entity>();

	public BossDisplay(BossBattles plugin, BattleBoss boss, Location location)
	{
		Plugin = plugin;
		_boss = boss;
		_bossLocation = location;
	}

	@EventHandler
	public void onEntityInteractEvent(PlayerInteractEntityEvent event)
	{
		if (_entities.contains(event.getRightClicked()))
		{
			setChosen(event.getPlayer());
		}
	}

	public void addEntity(Entity entity)
	{
		_entities.add(entity);
	}

	@EventHandler
	public void preventMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Entity entity : _entities)
		{
			Location loc = entity.getLocation();

			if (loc.distance(_bossLocation) > 0.1)
			{
				loc = _bossLocation.clone();
				loc.setY(entity.getLocation().getY());
				loc.setDirection(
						UtilAlg.getTrajectory2d(entity.getLocation(), loc));

				entity.teleport(loc);
			}
		}
	}

	public abstract String getDisplayName();

	public Location getLocation()
	{
		return _bossLocation;
	}

	public void setChosen(Player player)
	{
		Plugin.setPicked(player, _boss);
	}

	public void removeBoss()
	{
		for (Entity entity : _entities)
		{
			entity.remove();
		}
	}

	public abstract void start();

	public void spawnHologram()
	{
		_hologram = new Hologram(Plugin.getArcadeManager().getHologramManager(),
				getHologramLocation(), getDisplayName());

		_hologram.start();
	}

	public Hologram getHologram()
	{
		return _hologram;
	}

	public void removeHologram()
	{
		_hologram.stop();
	}

	public abstract Location getHologramLocation();
}
