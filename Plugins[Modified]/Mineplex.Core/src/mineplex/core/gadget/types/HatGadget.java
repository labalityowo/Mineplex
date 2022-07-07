package mineplex.core.gadget.types;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.hat.HatType;

public abstract class HatGadget extends OutfitGadget
{

	private final HatType _hatType;

	public HatGadget(GadgetManager manager, HatType type)
	{
		super(manager, type.getName(), type.getLore(), type.getCost(), ArmorSlot.HELMET, Material.GLASS, (byte) 0, type.getAltNames());

		_hatType = type;
		setDisplayItem(type.getHat());
	}

	@Override
	public GadgetType getGadgetType()
	{
		return GadgetType.HAT;
	}

	public HatType getHatType()
	{
		return _hatType;
	}

}
