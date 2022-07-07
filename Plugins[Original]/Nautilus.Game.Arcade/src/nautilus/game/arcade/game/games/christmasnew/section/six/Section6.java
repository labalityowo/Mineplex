package nautilus.game.arcade.game.games.christmasnew.section.six;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.MapUtil;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;

public class Section6 extends Section
{

	private static final int GATE_OPEN_DELAY = 600;

	private final List<Location> _gate;

	public Section6(ChristmasNew host, Location sleighTarget, Location... presents)
	{
		super(host, sleighTarget);

		_gate = _worldData.GetCustomLocs(String.valueOf(Material.ENDER_STONE.getId()));
		_gate.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.NETHER_FENCE));

		setTimeSet(18000);

		registerChallenges(
				new BossFight(host, presents[0], this)
		);
	}

	@Override
	public void onRegister()
	{
		_host.sendSantaMessage("Oh dear, a storm is coming. Stay close everyone!", ChristmasNewAudio.SANTA_STORM);

		_host.WorldWeatherEnabled = true;
		_worldData.World.setStorm(true);

		AtomicInteger lowestY = new AtomicInteger(Integer.MAX_VALUE);

		for (Location location : _gate)
		{
			if (lowestY.get() > location.getBlockY())
			{
				lowestY.set(location.getBlockY());
			}
		}

		_host.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				int y = lowestY.getAndIncrement();

				_gate.removeIf(location ->
				{
					if (location.getBlockY() == y)
					{
						MapUtil.QuickChangeBlockAt(location, Material.AIR);
						return true;
					}

					return false;
				});

				if (_gate.isEmpty())
				{
					cancel();
				}
				else
				{
					Location location = _gate.get(0);

					location.getWorld().playSound(location, Sound.PISTON_RETRACT, 1.5F, 0.8F);
				}
			}
		}, GATE_OPEN_DELAY, 5);
	}

	@Override
	public void onUnregister()
	{

	}

	@Override
	public boolean isComplete()
	{
		return false;
	}

	@Override
	public void onSantaTarget()
	{
		_host.sendSantaMessage("Jeepers Creepers! It’s the Pumpkin King’s Castle.", ChristmasNewAudio.SANTA_PUMPKIN_CASTLE);
	}
}
