package mineplex.core.particleeffects;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;

public class LoveDoctorHitEffect extends Effect
{

	public LoveDoctorHitEffect(Player player)
	{
		super(200, new EffectLocation(player.getLocation()));
	}

	@Override
	public void runEffect()
	{
		Location location = getEffectLocation().getLocation();
		UtilParticle.PlayParticle(UtilParticle.ParticleType.HEART, location.clone().add(0, 1, 0), 0.75f, 0.75f, 0.75f, 0, 1, UtilParticle.ViewDist.NORMAL);
	}

}
