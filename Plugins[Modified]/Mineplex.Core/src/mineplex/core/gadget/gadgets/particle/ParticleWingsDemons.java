package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
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

public class ParticleWingsDemons extends ParticleGadget
{

	private ShapeWings _wings = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(0.2,0.2,0.2), 1, 0, false, ShapeWings.DEFAULT_ROTATION, ShapeWings.ANGEL_WING_PATTERN);
	private ShapeWings _wingsEdge = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(0.1,0.1,0.1), 1, 0, true, ShapeWings.DEFAULT_ROTATION, ShapeWings.ANGEL_WING_PATTERN);

	public ParticleWingsDemons(GadgetManager manager)
	{
		super(manager, "Demon Wings",
				UtilText.splitLineToArray(C.cGray + C.Italics + "\"I'm just excited to see my Lord and Savior Baphomet represented in such glorious Italian stone.\"", LineFormat.LORE),
				-2, Material.COAL, (byte) 1);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location loc = player.getLocation().add(0, 1.2, 0).add(player.getLocation().getDirection().multiply(-0.2));
		if (Manager.isMoving(player))
		{
			if (event.getType() == UpdateType.FASTEST)
			    UtilParticle.playParticleFor(player, ParticleType.SMOKE, loc, 0.3f, 0.2f, 0.3f, 0, 10, ViewDist.NORMAL);
			return;
		}

		if (event.getType() == UpdateType.FAST) _wings.display(loc);
		if (event.getType() == UpdateType.FAST) _wingsEdge.display(loc);

	}

}
