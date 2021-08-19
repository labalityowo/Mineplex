package mineplex.core.gadget.gadgets.death;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;

public class DeathFrostLord extends DeathEffectGadget
{
	public DeathFrostLord(GadgetManager manager)
	{
		super(manager, "Fall of the Frost Lord", 
				UtilText.splitLineToArray(C.cGray + "The power of Winter must eventually give way to Spring.", LineFormat.LORE),
				-3,
				Material.SNOW_BALL, (byte)0, "Frost Lord");
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.SNOW_BALL, (byte)0);
	}
}
