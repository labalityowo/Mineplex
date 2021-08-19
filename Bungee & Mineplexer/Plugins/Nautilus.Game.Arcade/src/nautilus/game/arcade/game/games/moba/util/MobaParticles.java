package nautilus.game.arcade.game.games.moba.util;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import org.bukkit.entity.LivingEntity;

public class MobaParticles
{

	public static void healing(LivingEntity entity, int amount)
	{
		UtilParticle.PlayParticleToAll(ParticleType.HEART, entity.getLocation().add(0, 1.3, 0), 0.5F, 0.5F, 0.5F, 0.1F, amount, ViewDist.LONG);
	}

}
