package mineplex.core.gadget.gadgets.outfit.spacesuit;

import mineplex.core.gadget.GadgetManager;

import org.bukkit.Material;

public class OutfitSpaceSuitLeggings extends OutfitSpaceSuit
{

	public OutfitSpaceSuitLeggings(GadgetManager manager) 
	{
		super(manager, "Space Pants", -2, ArmorSlot.LEGS, Material.GOLD_LEGGINGS, (byte)0);
	}

}
