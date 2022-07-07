package mineplex.minecraft.game.core.boss.slimeking.ability;

import java.util.HashMap;
import java.util.Map;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.boss.slimeking.creature.SlimeCreature;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class AbsorbAbility extends SlimeAbility
{
	private int _ticksPerPulse;
	private int _pulseMax;
	private int _pulseCount;
	private int _maxDistance;

	public AbsorbAbility(SlimeCreature slime)
	{
		super(slime);
		_ticksPerPulse = 20;
		_pulseMax = 10;
		_pulseCount = 0;
		_maxDistance = 20;
	}

	@Override
	public void tickCustom()
	{
		if (getTicks() % _ticksPerPulse == 0)
		{
			pulse();
			_pulseCount++;

			if (_pulseCount >= _pulseMax)
			{
				setIdle(true);
			}
		}
	}

	private void pulse()
	{
		HashMap<Player, Double> playerMap = UtilPlayer.getInRadius(getSlime().getEntity().getLocation(), _maxDistance);

		for (Map.Entry<Player, Double> entry : playerMap.entrySet())
		{
			Player player = entry.getKey();
			double distance = entry.getValue();

			Vector dir = UtilAlg.getTrajectory2d(player, getSlime().getEntity());
			dir.multiply(0.4);
			dir.setY(0.1);
			player.setVelocity(dir);
			getSlime().getEvent().getDamageManager().NewDamageEvent(player, getSlime().getEntity(), null,
					EntityDamageEvent.DamageCause.MAGIC, getDamage(distance), false, false, false, getSlime().getEvent().getName(), "Absorb");
		}
	}

	private double getDamage(double distance)
	{
		double mult = _maxDistance - distance;

		return mult * 0.25;
	}
}
