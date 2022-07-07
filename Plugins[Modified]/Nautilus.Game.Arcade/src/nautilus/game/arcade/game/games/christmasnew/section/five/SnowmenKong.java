package nautilus.game.arcade.game.games.christmasnew.section.five;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;

class SnowmenKong extends SectionChallenge
{

	private static final int SEARCH_DISTANCE_SQUARED = 100;
	private static final int MIN_TICKS_BEFORE_HIT = 8;
	private static final Vector HIT_VELOCITY = new Vector(-1.5, 0.7, 0);
	private static final int MAX_TICKS = 200;
	private static final int MAX_MOBS = 120;
	private static final long WAVE_FREQUENCY = TimeUnit.SECONDS.toMillis(3);

	private final List<SnowmanWave> _waves;

	private boolean _spawn;

	SnowmenKong(ChristmasNew host, Location present, Section section)
	{
		super(host, present, section);

		_waves = new ArrayList<>(3);

		createWaves(_worldData.GetCustomLocs(String.valueOf(Material.QUARTZ_ORE.getId())), false);
		createWaves(_worldData.GetCustomLocs(String.valueOf(Material.MELON_BLOCK.getId())), true);
	}

	@Override
	public void onPresentCollect()
	{
		_spawn = false;
		_entities.forEach(Entity::remove);
		_entities.clear();
	}

	@Override
	public void onRegister()
	{
		_host.getArcadeManager().runSyncLater(() -> _spawn = true, Section5.TICKS_TO_DELAY);
	}

	@Override
	public void onUnregister()
	{

	}

	private void createWaves(List<Location> locations, boolean positiveZ)
	{
		locations.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));

		locations = new ArrayList<>(locations);

		while (!locations.isEmpty())
		{
			SnowmanWave wave = new SnowmanWave(positiveZ);
			Location start = locations.get(0);

			locations.removeIf(location ->
			{
				if (UtilMath.offsetSquared(start, location) < SEARCH_DISTANCE_SQUARED)
				{
					location.setYaw(positiveZ ? 0 : 180);
					wave.Spawns.add(location);
					return true;
				}

				return false;
			});

			_waves.add(wave);
		}
	}

	@EventHandler
	public void updateWaveSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !_spawn || _entities.size() > MAX_MOBS)
		{
			return;
		}

		for (SnowmanWave wave : _waves)
		{
			int index = UtilMath.r(wave.Spawns.size() - 1) + 1;
			boolean upShift = Math.random() > 0.5;

			for (int i = 0; i < wave.Spawns.size(); i++)
			{
				if (i == index || i == (index + (upShift ? 1 : -1)))
				{
					continue;
				}

				Location location = wave.Spawns.get(i);
				Snowman snowman = spawn(location, Snowman.class);

				UtilEnt.vegetate(snowman);
				UtilEnt.ghost(snowman, true, false);
				UtilEnt.setFakeHead(snowman, true);
				UtilEnt.setTickWhenFarAway(snowman, true);

				wave.Entities.add(snowman);
			}
		}
	}

	@EventHandler
	public void updateCollide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (SnowmanWave wave : _waves)
		{
			for (Snowman entity : wave.Entities)
			{
				if (!entity.isValid())
				{
					continue;
				}

				Location target = entity.getLocation().add(0, -1, (wave.PositiveZ ? 2 : -2));
				UtilEnt.CreatureMove(entity, target, 1.8F);

				if (entity.getTicksLived() < MIN_TICKS_BEFORE_HIT)
				{
					continue;
				}

				for (Player player : UtilEnt.getPlayersInsideEntity(entity))
				{
					if (UtilPlayer.isSpectator(player) || !Recharge.Instance.use(player, "Snowman Hit", 1000, false, false))
					{
						continue;
					}

					_host.getArcadeManager().GetDamage().NewDamageEvent(player, entity, null, DamageCause.CUSTOM, 4, false, true, true, entity.getName(), entity.getName());
					UtilAction.velocity(player, HIT_VELOCITY);
				}

				Location location = entity.getLocation();

				if (UtilBlock.solid(location.add(location.getDirection()).getBlock()) || entity.getTicksLived() > MAX_TICKS)
				{
					entity.remove();
				}
			}
		}
	}

	private class SnowmanWave
	{
		private final List<Location> Spawns;
		private final List<Snowman> Entities;
		private final boolean PositiveZ;

		SnowmanWave(boolean positiveZ)
		{
			Spawns = new ArrayList<>();
			Entities = new ArrayList<>();
			PositiveZ = positiveZ;
		}
	}
}
