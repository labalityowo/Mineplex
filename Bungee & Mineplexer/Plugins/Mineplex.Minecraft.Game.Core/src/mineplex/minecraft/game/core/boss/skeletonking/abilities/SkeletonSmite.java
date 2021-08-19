package mineplex.minecraft.game.core.boss.skeletonking.abilities;

import java.math.BigDecimal;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.skeletonking.SkeletonCreature;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkeletonSmite extends BossAbility<SkeletonCreature, Skeleton>
{
	private static final long TOTAL_ATTACK_DURATION = 8000;
	private long _start;
	private int _ticks;
	private boolean _shot;
	
	public SkeletonSmite(SkeletonCreature creature)
	{
		super(creature);
		_start = System.currentTimeMillis();
		_ticks = 0;
		_shot = false;
	}
	
	@Override
	public int getCooldown()
	{
		return 15;
	}

	@Override
	public boolean canMove()
	{
		return false;
	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

	@Override
	public boolean hasFinished()
	{
		return UtilTime.elapsed(_start, TOTAL_ATTACK_DURATION) && _shot;
	}

	@Override
	public void setFinished()
	{
		_start = System.currentTimeMillis() - TOTAL_ATTACK_DURATION;
		_shot = true;
	}

	@Override
	public void tick()
	{
		if (_shot)
			return;
		
		if (_ticks < (6 * 20))
		{
			_ticks++;
			double maxHeight = Math.min(_ticks / 20, 6);
			int radius = Math.max(6 - (new BigDecimal(_ticks).divide(new BigDecimal(20)).intValue()), 0);

			for (double y = 0; y < maxHeight; y += 0.5)
			{
				double cos = radius * Math.cos(y);
				double sin = radius * Math.sin(y);

				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getEntity().getLocation().add(cos, y, sin), null, 0, 1, ViewDist.MAX);
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getEntity().getLocation().add(sin, y, cos), null, 0, 1, ViewDist.MAX);
			}
		}
		else
		{
			_shot = true;
			for (Player player : UtilPlayer.getInRadius(getEntity().getLocation(), 15).keySet())
			{
				if (player.isDead() || !player.isValid() || !player.isOnline())
				{
					continue;
				}
				if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
				{
					continue;
				}
				
				player.getWorld().strikeLightningEffect(player.getLocation());
				getBoss().getEvent().getDamageManager().NewDamageEvent(player, getEntity(), null, DamageCause.LIGHTNING, 15 * getBoss().getDifficulty(), false, true, false, getEntity().getName(), "Lightning Strike");
			}
		}
	}
}