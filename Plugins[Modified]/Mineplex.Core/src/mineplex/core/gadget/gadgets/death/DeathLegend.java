package mineplex.core.gadget.gadgets.death;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class DeathLegend extends DeathEffectGadget
{

	public DeathLegend(GadgetManager manager)
	{
		super(manager, "Legend's Legacy",
				UtilText.splitLineToArray(C.cGray + "A story to be told for generations to come.", LineFormat.LORE),
				CostConstants.UNLOCKED_WITH_LEGEND,
				Material.ENDER_PORTAL_FRAME, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.ENDER_PEARL, (byte) 0);
		event.getLocation().getWorld().playEffect(event.getLocation(), Effect.ENDER_SIGNAL, 0);
	}

}