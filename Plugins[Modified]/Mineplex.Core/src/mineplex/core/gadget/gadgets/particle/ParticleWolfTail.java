package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.util.CostConstants;

public class ParticleWolfTail extends ParticleImageGadget
{

	private static final String[] PARTICLE =
		{
				"-1,-1,-1/121,121,121/119,119,119/123,123,123/128,128,128/-1,-1,-1/-1,-1,-1/-1,-1,-1/",
				"100,100,100/109,109,109/116,116,116/122,122,122/129,129,129/143,143,143/157,157,157/-1,-1,-1/",
				"92,92,92/100,100,100/113,113,113/118,118,118/127,127,127/143,143,143/167,167,167/194,194,194/",
				"91,91,91/102,102,102/110,110,110/120,120,120/128,128,128/144,144,144/171,171,171/204,204,204/",
				"96,96,96/101,101,101/108,108,108/-1,-1,-1/-1,-1,-1/-1,-1,-1/187,187,187/221,221,221/",
				"-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/244,244,244/",
		};

	public ParticleWolfTail(GadgetManager manager)
	{
		super(manager, "Wolf Tail", UtilText.splitLineToArray(C.cGray + "On all levels except physical.", LineFormat.LORE), PARTICLE, CostConstants.FOUND_IN_TREASURE_CHESTS, Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.WOLF));

		_yOffset = 0.5;
	}
}
