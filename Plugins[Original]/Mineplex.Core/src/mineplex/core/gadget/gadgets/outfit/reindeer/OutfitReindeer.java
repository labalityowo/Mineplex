package mineplex.core.gadget.gadgets.outfit.reindeer;

import org.bukkit.Color;
import org.bukkit.Material;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.gadget.util.CostConstants;

class OutfitReindeer extends OutfitGadget
{

	private static final String[] DESCRIPTION = UtilText.splitLineToArray(C.cGray + "Ever wanted to fly through the night on Christmas Eve like Rudolf? Well now you can!", LineFormat.LORE);

	OutfitReindeer(GadgetManager manager, String name, ArmorSlot slot, Material mat, byte data, String... altNames)
	{
		super(manager, "Reindeer " + name, DESCRIPTION, CostConstants.FOUND_IN_GINGERBREAD_CHESTS, slot, mat, data, altNames);

		setColor(Color.MAROON);
	}
}
