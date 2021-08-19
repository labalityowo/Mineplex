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

public class MountMule extends HorseMount
{
	public MountMule(GadgetManager manager)
	{
		super(manager,
				"Mount Mule",
				UtilText.splitLineToArray(C.cGray + "Your very own trusty pack mule!", LineFormat.LORE),
				3000,
				Material.HAY_BLOCK,
				(byte) 0,
				Color.BLACK,
				Style.BLACK_DOTS,
				Variant.MULE,
				1,
				null
		);
	}
}
