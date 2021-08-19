package mineplex.core.gadget.gadgets.outfit.windup;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitWindupChestplate extends OutfitWindupSuit
{

	public OutfitWindupChestplate(GadgetManager manager)
	{
		super(manager, "Chestplate", ArmorSlot.CHEST, Material.LEATHER_CHESTPLATE);
	}
}
