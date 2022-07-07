package mineplex.core.gadget.gadgets.outfit.freezesuit;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitFreezeSuitHelmet extends OutfitFreezeSuit
{

	public OutfitFreezeSuitHelmet(GadgetManager manager)
	{
		super(manager, "Freeze Helmet", -16, ArmorSlot.HELMET, Material.ICE, (byte) 0);
	}

}
