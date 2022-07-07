package mineplex.core.gadget.set.suits;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitBoots;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.ravesuit.OutfitRaveSuitLeggings;
import mineplex.core.gadget.types.GadgetSet;

public class SetRaveSuit extends GadgetSet
{
	public SetRaveSuit(GadgetManager manager)
	{
		super(manager, "Rave Suit", "Hyper Speed",
				manager.getGadget(OutfitRaveSuitHelmet.class),
				manager.getGadget(OutfitRaveSuitChestplate.class),
				manager.getGadget(OutfitRaveSuitLeggings.class),
				manager.getGadget(OutfitRaveSuitBoots.class));
	}
}
