package mineplex.core.particleeffects;

import org.bukkit.Location;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;

public class WitchParticleEffect extends Effect
{

	public WitchParticleEffect(Location location)
	{
		super(-1, new EffectLocation(location));
		_period = 5;
	}

	@Override
	public void runEffect()
	{
		UtilParticle.PlayParticle(UtilParticle.ParticleType.WITCH_MAGIC, _effectLocation.getLocation().clone(),
				1f, 1f, 1f, 0f, 20,
				UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
	}

}
