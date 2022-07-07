package mineplex.core.rankGiveaway;

import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LightFlicker extends BukkitRunnable
{
	private static int MAX_TICKS = 100;

	private World _world;
	private Random _random;
	private int _ticks;
	private long _previousTime;

	public LightFlicker(World world)
	{
		_world = world;
		_random = new Random();
		_ticks = 0;
		_previousTime = world.getTime();
	}

	@Override
	public void run()
	{
		if (_ticks >= MAX_TICKS)
		{
			_world.setTime(_previousTime);
			cancel();
			return;
		}

		if (_ticks % 5 == 0)
		{
			long time = (long) (24000 * _random.nextDouble());
			_world.setTime(time);
		}

		if (_ticks % 10 == 0)
		{
			for (Player player : _world.getPlayers())
			{
				player.playSound(player.getEyeLocation(), Sound.AMBIENCE_THUNDER, 1, 1);
			}
		}

		_ticks++;
	}
}
