package mineplex.core.gadget.set.suits;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksBoots;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksChestplate;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksHat;
import mineplex.core.gadget.gadgets.outfit.stpatricks.OutfitStPatricksLeggings;
import mineplex.core.gadget.types.GadgetSet;

public class SetStPatricksSuit extends GadgetSet
{

	public SetStPatricksSuit(GadgetManager manager)
	{
		super(manager, "St Patrick's Suit", "You're so rich that gold falls out of your pocket with every step!",
				manager.getGadget(OutfitStPatricksHat.class),
				manager.getGadget(OutfitStPatricksChestplate.class),
				manager.getGadget(OutfitStPatricksLeggings.class),
				manager.getGadget(OutfitStPatricksBoots.class));
	}

}
