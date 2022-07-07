package mineplex.core.gadget.gadgets.outfit.stpatricks;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitStPatricksChestplate extends OutfitStPatricksSuit
{

	public OutfitStPatricksChestplate(GadgetManager manager)
	{
		super(manager, "Leprechaun's Chest", -18, ArmorSlot.CHEST, Material.LEATHER_CHESTPLATE, (byte) 0, "St Patrick's Chest");
	}

}