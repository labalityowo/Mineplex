package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleRain extends ParticleGadget
{

	public ParticleRain(GadgetManager manager)
	{
		super(manager, "Rain Cloud", UtilText
		        .splitLineToArray(C.cGray + "Bring your sadness wherever you go, with your very own portable rain cloud!", LineFormat.LORE), -2,
		        Material.INK_SACK, (byte) 4);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		if (Manager.isMoving(player))
		{
			UtilParticle.playParticleFor(player, ParticleType.SPLASH, player.getLocation(), 0.2f, 0, 0.2f, 0, 6, ViewDist.NORMAL);
		}
		else
		{
			Location loc = player.getLocation().add(0, 3.5, 0);
			UtilParticle.PlayParticle(ParticleType.EXPLODE, loc, 0.6f, 0f, 0.6f, 0, 5, ViewDist.NORMAL, player);
			UtilParticle.PlayParticle(ParticleType.CLOUD, loc, 0.6f, 0.1f, 0.6f, 0, 5, ViewDist.NORMAL);

			UtilParticle.playParticleFor(player, ParticleType.DRIP_WATER, loc, 0.4f, 0.1f, 0.4f, 0, 1, ViewDist.NORMAL);

			// Sound
			player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_RAIN, 0.1f, 1f);
		}
	}
}
