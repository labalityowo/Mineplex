package mineplex.core.gadget.gadgets.outfit.freezesuit;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitFreezeSuitBoots extends OutfitFreezeSuit
{

	public OutfitFreezeSuitBoots(GadgetManager manager)
	{
		super(manager, "Freeze Boots", -16, ArmorSlot.BOOTS, Material.LEATHER_BOOTS, (byte) 0);
	}

}
