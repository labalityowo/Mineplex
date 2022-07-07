package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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

public class ParticleBlood extends ParticleGadget
{

	public ParticleBlood(GadgetManager manager)
	{
		super(manager, "Blood Helix", UtilText.splitLineToArray(C.cGray + "Blood magic is very dangerous... but also very cool!", LineFormat.LORE),
		        -2, Material.REDSTONE, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		if (Manager.isMoving(player))
		{
			UtilParticle.playParticleFor(player, ParticleType.RED_DUST, player.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 0, 2, ViewDist.NORMAL);
		}
		else
		{

			int ticks = 10;
			int hticks = 40;
			boolean up = player.getTicksLived() % (hticks * 2) < hticks;
			int tick = player.getTicksLived() % ticks;
			double htick = player.getTicksLived() % hticks;
			int splits = 4;

			Location loc = player.getLocation().add(0, 2, 0);

			for (double d = tick * (Math.PI * 2 / splits) / ticks; d < Math.PI * 2; d += Math.PI * 2 / splits)
			{
				Vector v = new Vector(Math.sin(d), 0, Math.cos(d));
				v.normalize().multiply(Math.max(0.2, Math.sin((htick / hticks) * Math.PI) * 1.0));
				v.setY((htick / hticks) * -2);
				if (up) v.setY(-2 + 2 * (htick / hticks));

				Location lloc = loc.clone().add(v);

				UtilParticle.playParticleFor(player, ParticleType.RED_DUST, lloc, null, 0f, 2, ViewDist.NORMAL);
			}
		}
	}
}
