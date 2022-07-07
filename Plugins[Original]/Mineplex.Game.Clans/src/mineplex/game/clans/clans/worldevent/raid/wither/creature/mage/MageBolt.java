package mineplex.game.clans.clans.worldevent.raid.wither.creature.mage;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;

public class MageBolt extends BossPassive<UndeadMage, Skeleton>
{
	private static final double RANGE = 10;
	private long _lastUsed;
	
	public MageBolt(UndeadMage creature)
	{
		super(creature);
		_lastUsed = System.currentTimeMillis();
	}
	
	private void shootBolt(Player target)
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

			UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, newTarget, 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
		}
		
		if (canHit)
		{
			getBoss().getEvent().getDamageManager().NewDamageEvent(target, getEntity(), null, DamageCause.CUSTOM, 5, true, true, false, getEntity().getName(), "Mystical Energy");
		}
	}
	
	@Override
	public int getCooldown()
	{
		return 3;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
		if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
		{
			Player target = UtilMath.randomElement(getBoss().getPlayers(UtilPlayer.getInRadius(getLocation(), RANGE), RANGE));
			if (target != null)
			{
				_lastUsed = System.currentTimeMillis();
				shootBolt(target);
			}
		}
	}
}