package mineplex.core.gadget.gadgets.outfit.spacesuit;

import mineplex.core.gadget.GadgetManager;

import org.bukkit.Material;

public class OutfitSpaceSuitBoots extends OutfitSpaceSuit
{

	public OutfitSpaceSuitBoots(GadgetManager manager) 
	{
		super(manager, "Space Boots", -2, ArmorSlot.BOOTS, Material.GOLD_BOOTS, (byte)0);
	}

}
