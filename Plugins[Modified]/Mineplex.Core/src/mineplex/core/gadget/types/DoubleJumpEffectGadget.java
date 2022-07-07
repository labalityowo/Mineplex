package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.gadget.GadgetManager;

public abstract class DoubleJumpEffectGadget extends Gadget
{

	public DoubleJumpEffectGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, String... altNames)
	{
		super(manager, GadgetType.DOUBLE_JUMP, name, desc, cost, mat, data, 1, altNames);
	}

	public abstract void doEffect(Player player);
}
