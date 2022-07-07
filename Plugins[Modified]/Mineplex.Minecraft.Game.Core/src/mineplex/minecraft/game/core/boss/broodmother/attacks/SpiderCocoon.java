package mineplex.minecraft.game.core.boss.broodmother.attacks;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.Location;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;

public class SpiderCocoon extends BossAbility<SpiderCreature, Spider>
{
	private static final long ABILITY_DURATION = 7000;
	private long _start;
	
	public SpiderCocoon(SpiderCreature creature)
	{
		super(creature);
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
		return UtilTime.elapsed(_start, ABILITY_DURATION);
	}

	@Override
	public void setFinished()
	{
		_start = System.currentTimeMillis() - ABILITY_DURATION;
	}

	@Override
	public void tick()
	{
		for (Location loc : UtilShapes.getSphereBlocks(getEntity().getLocation(), 3, 3, false))
		{
			UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, loc, null, 0, 1, ViewDist.NORMAL);
		}
	}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (hasFinished())
		{
			return;
		}
		if (event.GetDamageeEntity() != null && event.GetDamageeEntity().equals(getEntity()))
		{
			event.SetCancelled("Cocoon Defense");
		}
	}
}