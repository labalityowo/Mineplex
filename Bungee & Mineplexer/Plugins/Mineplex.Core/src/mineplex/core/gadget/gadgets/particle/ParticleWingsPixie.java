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

public class ParticleWingsPixie extends ParticleGadget
{
	
	private ShapeWings _wings = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(1/300d,1,1), 1, 0, false);
	private ShapeWings _wingsEdge = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(1,105/255d,180/255d), 1, 0, true);

	public ParticleWingsPixie(GadgetManager manager)
	{
		super(manager, "Pixie Wings", UtilText.splitLineToArray(C.cGray + "Most kinds of pixies steal coins from weary travelers, but this kind just looks fabulous.", LineFormat.LORE),
				-2, Material.INK_SACK, (byte) 12);
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
			return;
		}

		if (event.getType() == UpdateType.FAST) _wings.display(loc);
		if (event.getType() == UpdateType.FAST) _wingsEdge.display(loc);

		
	}

}
