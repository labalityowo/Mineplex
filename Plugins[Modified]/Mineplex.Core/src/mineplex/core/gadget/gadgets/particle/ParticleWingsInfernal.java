package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleWingsInfernal extends ParticleGadget
{
	
	private ShapeWings _wings = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(1,0,0), 1, 0, false);
	private ShapeWings _wingsFlame = new ShapeWings(ParticleType.FLAME.particleName, new Vector(0,0,0), 1, 0, false);
	private ShapeWings _wingsEdge = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(1,0.6,0), 1, 0, true);

	public ParticleWingsInfernal(GadgetManager manager)
	{
		super(manager, "Infernal Wings", UtilText.splitLineToArray(C.cGray + C.Italics + "\"I didn't take your mail Mrs. Pemberton! Stop asking me that. Leave me alone! I don't know. Satan's pretty cool!\"", LineFormat.LORE),
				-2, Material.MAGMA_CREAM, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{	
		Location loc = player.getLocation().add(0, 1.2, 0).add(player.getLocation().getDirection().multiply(-0.2));
		if (Manager.isMoving(player))
		{
			if (event.getType() == UpdateType.TICK) 
			{
				_wings.displayParticle(loc);
				_wingsEdge.displayParticle(loc);
			}
			if(event.getType() == UpdateType.FASTEST)
			{
				_wingsFlame.displayParticle(loc);
			}
			return;
		}

		if (event.getType() == UpdateType.FAST) _wings.display(loc);
		if (event.getType() == UpdateType.SEC) _wingsFlame.display(loc);
		if (event.getType() == UpdateType.FAST) _wingsEdge.display(loc);
	}

}
