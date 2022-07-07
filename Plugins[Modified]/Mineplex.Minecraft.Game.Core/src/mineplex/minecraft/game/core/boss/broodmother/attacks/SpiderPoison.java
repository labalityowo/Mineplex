package mineplex.minecraft.game.core.boss.broodmother.attacks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.minecraft.game.core.boss.EventCreature;

public class SpiderPoison implements IThrown
{
	private EventCreature _boss;

	public SpiderPoison(EventCreature boss, Entity item)
	{
		_boss = boss;

		boss.getEvent().getProjectileManager().AddThrow(item, boss.getEntity(), this, -1, true, true, true, false, 2f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target instanceof Player)
		{
			_boss.getEvent().getCondition().Factory()
					.Poison("Brood Mother Poison", target, _boss.getEntity(), 2, 1, false, true, false);

			_boss.getEvent()
					.getDamageManager()
					.NewDamageEvent(target, _boss.getEntity(), null, DamageCause.PROJECTILE, 2, true, false, false,
							"Brood Mother Poison", "Brood Mother Poison");
		}

		burst(data);
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		burst(data);
	}

	private void burst(ProjectileUser data)
	{
		data.getThrown().remove();

		UtilParticle.PlayParticle(ParticleType.ICON_CRACK.getParticle(Material.SLIME_BALL, 0), data.getThrown().getLocation(),
				0.3F, 0.3F, 0.3F, 0, 30, ViewDist.NORMAL, UtilServer.getPlayers());
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		burst(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}