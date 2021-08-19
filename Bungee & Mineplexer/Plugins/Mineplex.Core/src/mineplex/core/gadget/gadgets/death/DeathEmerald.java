package mineplex.core.gadget.gadgets.death;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;

public class DeathEmerald extends DeathEffectGadget
{

	public DeathEmerald(GadgetManager manager)
	{
		super(manager, "Green Death", 
				UtilText.splitLineToArray(C.cGreen + "Watch your power fade into an emerald mist.", LineFormat.LORE),
				-2, Material.EMERALD, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.EMERALD, (byte) 0);
	}

}
