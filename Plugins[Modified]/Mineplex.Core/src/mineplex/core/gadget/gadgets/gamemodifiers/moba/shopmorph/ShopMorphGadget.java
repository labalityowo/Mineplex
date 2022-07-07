package mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph;

import org.bukkit.Material;

import mineplex.core.common.util.C;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemStackFactory;

public class ShopMorphGadget extends GameModifierGadget
{

	private final ShopMorphType _type;

	public ShopMorphGadget(GadgetManager manager, GameCosmeticCategory category, ShopMorphType type)
	{
		this(manager, category, type, CostConstants.FOUND_IN_MOBA_CHESTS);
	}

	public ShopMorphGadget(GadgetManager manager, GameCosmeticCategory category, ShopMorphType type, int cost)
	{
		super(manager, category, type.getName(), new String[] {
				C.cGray + "Changes the Gold Upgrades NPC",
				C.cGray + "to " + (type.getSkinData() == null ? "a " + type.getName() : type.getName()) + ".",
		}, cost, Material.GLASS, (byte) 0);

		_type = type;
		setDisplayItem(type.getSkinData() == null ? ItemStackFactory.Instance.CreateStack(type.getMaterial(), type.getMaterialData()) : type.getSkinData().getSkull());
	}

	public ShopMorphType getType()
	{
		return _type;
	}
}
