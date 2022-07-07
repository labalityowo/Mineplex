package mineplex.core.gadget.gadgets.outfit.freezesuit;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitFreezeSuitChestplate extends OutfitFreezeSuit
{

	public OutfitFreezeSuitChestplate(GadgetManager manager)
	{
		super(manager, "Freeze Chest", -16, ArmorSlot.CHEST, Material.LEATHER_CHESTPLATE, (byte) 0);
	}

}
