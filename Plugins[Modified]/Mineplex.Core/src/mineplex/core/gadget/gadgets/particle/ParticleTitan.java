package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
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

public class ParticleTitan extends ParticleGadget
{

	public ParticleTitan(GadgetManager manager)
	{
		super(manager, "Flame of the Titans",
		        UtilText.splitLineToArray(C.cGray + "These flames are said to be the spirit of a Titan.", LineFormat.LORE), -1, Material.FIREBALL,
		        (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)

		    return;

		int tick = player.getTicksLived() % 620;

		double total = 3;

		if (getSet() != null && getSet().isActive(player)) total = 4;

		double step = (1 / total) * Math.PI * 2;
		double offset1 = (step / 20) * tick % 20;
		double offset2 = (step / 31) * tick % 31;

		double r = 3;

		if (Manager.isMoving(player))
		{
			UtilParticle.playParticleFor(player, ParticleType.FLAME, player.getLocation().add(0, 0.1, 0), 0.2f, 0.1f, 0.2f, 0.015f, 3, ViewDist.NORMAL);
			return;
		}

		for (int i = 0; i < total; i++)
		{
			double rad = step * i + offset1;

			double x = Math.sin(rad);
			double y = 0;
			double z = Math.cos(rad);

			Vector v = new Vector(x, y, z).normalize();
			v.multiply(r);
			Location loc = player.getLocation();
			loc.add(0, 0.1, 0);

			loc.add(v);

			v.multiply(-1);
			UtilParticle.playParticleFor(player, ParticleType.FLAME, loc, v, 0.05f, 0, ViewDist.NORMAL);

			rad = -(step * i + offset2);
			x = Math.sin(rad);
			z = Math.cos(rad);

			v = new Vector(x, y, z).normalize();
			v.multiply(r);
			loc = player.getLocation();
			loc.add(0, 0.1, 0);

			loc.add(v);

			v.multiply(-1);
			UtilParticle.playParticleFor(player, ParticleType.FLAME, loc, v, 0.05f, 0, ViewDist.NORMAL);
		}
	}

	@EventHandler
	public void titanOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.TITAN_PARTICLE_EFFECT))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}
}