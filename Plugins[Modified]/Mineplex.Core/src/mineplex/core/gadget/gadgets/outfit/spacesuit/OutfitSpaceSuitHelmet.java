package mineplex.core.gadget.gadgets.outfit.spacesuit;

import mineplex.core.gadget.GadgetManager;

import org.bukkit.Material;

public class OutfitSpaceSuitHelmet extends OutfitSpaceSuit
{

	public OutfitSpaceSuitHelmet(GadgetManager manager) 
	{
		super(manager, "Space Helmet", -2, ArmorSlot.HELMET, Material.GLASS, (byte)0);
	}

}
