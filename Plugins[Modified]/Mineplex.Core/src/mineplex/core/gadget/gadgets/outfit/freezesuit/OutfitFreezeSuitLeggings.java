package mineplex.core.gadget.gadgets.outfit.freezesuit;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitFreezeSuitLeggings extends OutfitFreezeSuit
{

	public OutfitFreezeSuitLeggings(GadgetManager manager)
	{
		super(manager, "Freeze Leggings", -16, ArmorSlot.LEGS, Material.LEATHER_LEGGINGS, (byte) 0);
	}

}
