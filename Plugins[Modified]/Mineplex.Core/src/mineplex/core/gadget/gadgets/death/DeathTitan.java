package mineplex.core.gadget.gadgets.death;

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

public class DeathTitan extends DeathEffectGadget
{
	public DeathTitan(GadgetManager manager)
	{
		super(manager, "Ashes of the Titans", 
				UtilText.splitLineToArray(C.cGray + "Even a Titan can fall in combat if their opponent is fierce enough.", LineFormat.LORE),
				-13,
				Material.FIREBALL, (byte)0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.FIREBALL, (byte)0);
	}
	
	@EventHandler
	public void titanOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.TITAN_DEATH_EFFECT))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}	
}