package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleFrostLord extends ParticleGadget
{
	public ParticleFrostLord(GadgetManager manager)
	{
		super(manager, "Wind of the Frost Lord",
		        UtilText.splitLineToArray(C.cGray + "He's not passing wind okay? HE HAS A CONDITION!", LineFormat.LORE), -3, Material.SNOW_BALL,
		        (byte) 0, "Frost Lord");
	}

	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		if (Manager.isMoving(player))
		{
			UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, player.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 0, 4, ViewDist.NORMAL,
			        UtilServer.getPlayers());
		}
		else
		{
			player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_RAIN, 0.015f, 0.2f);

			double scale = (double) (player.getTicksLived() % 50) / 50d;

			int amount = 4;

			if (getSet() != null && getSet().isActive(player)) amount = 6;

			double ang = Math.PI * 2 / amount;

			for (int i = 0; i < amount; i++)
			{
				double r = (1d - scale) * Math.PI * 2;

				double x = Math.sin(r + (i * ang)) * (r % (Math.PI * 4)) * 0.4;
				double z = Math.cos(r + (i * ang)) * (r % (Math.PI * 4)) * 0.4;

				UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, player.getLocation().add(x, scale * 3, z), 0f, 0f, 0f, 0, 1, ViewDist.NORMAL,
				        UtilServer.getPlayers());

				if (scale > 0.95 && Recharge.Instance.use(player, getName(), 1000, false, false))
				{
					UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, player.getLocation().add(0, scale * 3.5, 0), 0f, 0f, 0f, 0.2f, 60,
					        ViewDist.NORMAL, UtilServer.getPlayers());
					player.getWorld().playSound(player.getLocation(), Sound.STEP_SNOW, 1f, 1.5f);
				}
			}
		}
	}
}
