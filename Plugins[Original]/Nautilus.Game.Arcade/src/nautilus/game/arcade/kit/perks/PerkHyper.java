package nautilus.game.arcade.kit.perks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.recharge.Recharge;
import mineplex.core.common.util.UtilInv;
import nautilus.game.arcade.kit.Perk;

public class PerkHyper extends Perk
{
	public PerkHyper() 
	{
		super("Hyper", new String[] 
				{
				C.cYellow + "Right-click" + C.cGray + " with Sugar to " + C.cGreen + "GO HYPER"
				});
	}
	
	@EventHandler
	public void Interact(PlayerInteractEvent event)
	{
		if(!UtilEvent.isAction(event, ActionType.R)) return;
	
		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (event.getPlayer().getItemInHand().getType() != Material.SUGAR)
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.usable(player, "Hyper"))
			return;
		
		Recharge.Instance.useForce(player, "Hyper", 3000);
		
		event.setCancelled(true);
		
		UtilInv.remove(player, Material.SUGAR, (byte)0, 1);
		UtilInv.Update(player);
		
		Manager.GetCondition().Factory().Speed(GetName(), player, player, 4, 0, false, false, true);
	}
}
