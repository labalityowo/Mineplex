package mineplex.core.gadget.gadgets.gamemodifiers.gemhunters;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.gadget.util.CostConstants;

public class GemHuntersMountGadget extends GameModifierGadget
{

	private final GemHuntersMountType _type;

	// An alternative package name of "Clans " + name is due to mounts from the store originally just for clans being available on more platforms.
	public GemHuntersMountGadget(GadgetManager manager, GameCosmeticCategory category, GemHuntersMountType type)
	{
		super(manager, category, type.getName(), new String[]{
				C.cGray + "Changed your active Mount",
				C.cGray + "in " + F.greenElem("Gem Hunters") + " to " + type.getName() + ".",
		}, CostConstants.PURCHASED_FROM_STORE, type.getMaterial(), type.getMaterialData(), "Clans " + type.getName());

		_type = type;
	}

	public GemHuntersMountType getType()
	{
		return _type;
	}
}
