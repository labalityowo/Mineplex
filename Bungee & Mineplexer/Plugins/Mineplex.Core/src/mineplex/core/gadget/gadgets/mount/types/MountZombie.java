package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.util.CostConstants;

public class MountZombie extends HorseMount
{

	public MountZombie(GadgetManager manager)
	{
		super(manager, "Decrepit Warhorse", 
				UtilText.splitLinesToArray(new String[]{
						C.cGray + "Once a fierce warhorse, this undead beast will send fear into the hearts of your enemies.",
						"",
						C.cBlue + "Earned by defeating the Pumpkin King",
						C.cBlue + "in the 2015 Halloween Horror Event."
				}, LineFormat.LORE),
				CostConstants.NO_LORE,
				Material.ROTTEN_FLESH,
				(byte)0,
				Color.BLACK,
				Style.BLACK_DOTS,
				Variant.UNDEAD_HORSE,
				0.8,
				null
		);
	}
}
