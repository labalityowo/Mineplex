package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;

public class LifeSap extends BossPassive<CharlesWitherton, Wither>
{
	private static final double RANGE = 20;
	private long _lastUsed;
	private int _chargeTicks;
	
	public LifeSap(CharlesWitherton creature)
	{
		super(creature);
		_lastUsed = -1;
		_chargeTicks = -1;
	}
	
	private void shootBeam(Player target)
	{
		double curRange = 0;
		boolean canHit = true;
		
		while (curRange <= RANGE)
		{
			Location newTarget = getEntity().getEyeLocation().add(UtilAlg.getTrajectory(getEntity(), target).multiply(curRange));

			if (!UtilBlock.airFoliage(newTarget.getBlock()))
			{
				canHit = false;
				break;
			}
			if (UtilMath.offset(newTarget, target.getLocation()) <= 0.9)
			{
				break;
			}

			curRange += 0.2;

			UtilParticle.playColoredParticleToAll(Color.RED, ParticleType.RED_DUST, newTarget, 5, ViewDist.MAX);
		}
		
		if (canHit)
		{
			getBoss().getEvent().getDamageManager().NewDamageEvent(target, getEntity(), null, DamageCause.CUSTOM, 2.5, true, true, false, getEntity().getName(), "Mystical Energy");
			getBoss().setHealth(getBoss().getHealth() + 200);
		}
	}
	
	@Override
	public int getCooldown()
	{
		return 60;
	}
	
	@Override
	public boolean isProgressing()
	{
		return _chargeTicks != -1;
	}

	@Override
	public void tick()
	{
		if (_chargeTicks != -1)
		{
			_chargeTicks++;
			if (_chargeTicks >= (20 * 4))
			{
				_lastUsed = System.currentTimeMillis();
				_chargeTicks = -1;
				Player target = UtilPlayer.getClosest(getLocation(), RANGE);
				if (target != null)
				{
					shootBeam(target);
				}
			}
			else
			{
				for (Location loc : UtilShapes.getCircle(getEntity().getEyeLocation(), true, 1.5))
				{
					UtilParticle.playColoredParticleToAll(Color.RED, ParticleType.RED_DUST, loc, 2, ViewDist.MAX);
				}
			}
			return;
		}
		if (getBoss().getHealthPercent() <= 0.75)
		{
			if (_lastUsed == -1)
			{
				_lastUsed = System.currentTimeMillis();
			}
			else
			{
				if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
				{
					_chargeTicks = 0;
				}
			}
		}
	}
}