package mineplex.core.gadget.gadgets.outfit.spacesuit;

import mineplex.core.gadget.GadgetManager;

import org.bukkit.Material;

public class OutfitSpaceSuitChestplate extends OutfitSpaceSuit
{

	public OutfitSpaceSuitChestplate(GadgetManager manager) 
	{
		super(manager, "Space Jacket", -2, ArmorSlot.CHEST, Material.GOLD_CHESTPLATE, (byte)0);
	}

}
