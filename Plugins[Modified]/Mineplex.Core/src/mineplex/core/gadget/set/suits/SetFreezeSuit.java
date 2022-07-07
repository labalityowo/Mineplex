package mineplex.core.gadget.set.suits;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitBoots;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitChestplate;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitHelmet;
import mineplex.core.gadget.gadgets.outfit.freezesuit.OutfitFreezeSuitLeggings;
import mineplex.core.gadget.types.GadgetSet;

public class SetFreezeSuit extends GadgetSet
{

	public SetFreezeSuit(GadgetManager manager)
	{
		super(manager, "Freeze Suit", "Grants the wearer a \"snow aura\" and the ability to summon a bridge of ice.",
				manager.getGadget(OutfitFreezeSuitHelmet.class),
				manager.getGadget(OutfitFreezeSuitChestplate.class),
				manager.getGadget(OutfitFreezeSuitLeggings.class),
				manager.getGadget(OutfitFreezeSuitBoots.class));
	}

}
