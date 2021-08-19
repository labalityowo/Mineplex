package mineplex.core.gadget.set.suits;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitBoots;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.spacesuit.OutfitSpaceSuitLeggings;
import mineplex.core.gadget.types.GadgetSet;

public class SetSpaceSuit extends GadgetSet
{
	public SetSpaceSuit(GadgetManager manager)
	{
		super(manager, "Space Suit", "Low Gravity",
				manager.getGadget(OutfitSpaceSuitHelmet.class),
				manager.getGadget(OutfitSpaceSuitChestplate.class),
				manager.getGadget(OutfitSpaceSuitLeggings.class),
				manager.getGadget(OutfitSpaceSuitBoots.class));
	}
}
