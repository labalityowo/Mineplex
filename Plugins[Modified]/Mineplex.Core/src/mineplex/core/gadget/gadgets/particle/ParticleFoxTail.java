package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.util.CostConstants;

public class ParticleFoxTail extends ParticleImageGadget
{

	private static final String[] PARTICLE =
		{
				"-1,-1,-1/255,93,0/255,93,0/255,81,0/255,84,0/-1,-1,-1/-1,-1,-1/-1,-1,-1/",
				"255,96,0/255,90,0/255,81,0/246,75,0/246,76,2/255,115,51/255,195,169/-1,-1,-1/",
				"255,90,0/246,75,0/246,75,0/238,72,0/238,88,20/246,141,96/255,214,193/255,240,231/",
				"255,78,0/238,72,0/238,72,0/238,72,0/230,101,46/246,157,121/255,213,196/-1,-1,-1/",
				"246,75,0/230,70,0/238,72,0/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/",
		};

	public ParticleFoxTail(GadgetManager manager)
	{
		super(manager, "Fox Tail", UtilText.splitLineToArray(C.cGray + "What does the fox say? Well probably doesn't matter.", LineFormat.LORE), PARTICLE, CostConstants.FOUND_IN_TREASURE_CHESTS, Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.OCELOT));

		_yOffset = 0.5;
	}
}
