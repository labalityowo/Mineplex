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
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleFireRings extends ParticleGadget
{

	public ParticleFireRings(GadgetManager manager)
	{
		super(manager, "Flame Rings",
		        UtilText.splitLineToArray(C.cGray + "Forged from the blazing rods of 1000 Blazes by the infamous Nether King.", LineFormat.LORE),
		        -2, Material.BLAZE_ROD, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		if (Manager.isMoving(player))
		{
			UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 1f, 0), 0.2f, 0.2f, 0.2f, 0, 1, ViewDist.NORMAL,
			        UtilServer.getPlayers());
		}
		else
		{
			for (int i = 0; i < 1; i++)
			{
				double lead = i * ((2d * Math.PI) / 2);

				float x = (float) (Math.sin(player.getTicksLived() / 5d + lead) * 1f);
				float z = (float) (Math.cos(player.getTicksLived() / 5d + lead) * 1f);

				float y = (float) (Math.sin(player.getTicksLived() / 5d + lead) + 1f);

				UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(x, y, z), 0f, 0f, 0f, 0, 1, ViewDist.NORMAL,
				        UtilServer.getPlayers());
			}

			for (int i = 0; i < 1; i++)
			{
				double lead = i * ((2d * Math.PI) / 2);

				float x = (float) -(Math.sin(player.getTicksLived() / 5d + lead) * 1f);
				float z = (float) (Math.cos(player.getTicksLived() / 5d + lead) * 1f);

				float y = (float) (Math.sin(player.getTicksLived() / 5d + lead) + 1f);

				UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(x, y, z), 0f, 0f, 0f, 0, 1, ViewDist.NORMAL,
				        UtilServer.getPlayers());
			}

			// Sound
			player.getWorld().playSound(player.getLocation(), Sound.FIRE, 0.2f, 1f);
		}
	}
}
