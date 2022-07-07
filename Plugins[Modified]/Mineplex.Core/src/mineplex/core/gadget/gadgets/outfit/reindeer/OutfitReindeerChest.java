package mineplex.core.gadget.gadgets.outfit.reindeer;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitReindeerChest extends OutfitReindeer
{

	public OutfitReindeerChest(GadgetManager manager)
	{
		super(manager, "Chest", ArmorSlot.CHEST, Material.LEATHER_CHESTPLATE, (byte) 0);
	}
}
