package mineplex.core.gadget.gadgets.kitselector;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.KitSelectorGadget;

public class RainCloudKitSelector extends KitSelectorGadget
{

	public RainCloudKitSelector(GadgetManager manager)
	{
		super(manager, "Rain Cloud", UtilText.splitLinesToArray(new String[]{C.cGray + "The rain keeps falling, and the kit keeps calling."}, LineFormat.LORE),
				0, Material.POTION, (byte) 0);
	}


	@Override
	public void playParticle(Entity entity, Player playTo)
	{
		Location loc = entity.getLocation().add(0, 3.5, 0);
		UtilParticle.PlayParticle(UtilParticle.ParticleType.EXPLODE, loc, 0.6f, 0f, 0.6f, 0, 8, UtilParticle.ViewDist.NORMAL, playTo);

		UtilParticle.PlayParticle(UtilParticle.ParticleType.CLOUD, loc, 0.6f, 0.1f, 0.6f, 0, 8, UtilParticle.ViewDist.NORMAL, playTo);

		UtilParticle.PlayParticle(UtilParticle.ParticleType.DRIP_WATER, loc, 0.4f, 0.1f, 0.4f, 0, 2, UtilParticle.ViewDist.NORMAL, playTo);
	}

}
