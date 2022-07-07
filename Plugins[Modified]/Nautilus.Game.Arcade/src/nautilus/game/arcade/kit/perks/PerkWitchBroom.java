package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.kit.Perk;

public class PerkWitchBroom extends Perk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkWitchBroom() 
	{
		super("Broomstick", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " to use " + C.cGreen + "Broomstick"
				});
	}
	
	
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;
		
		//Start
		_active.put(player, System.currentTimeMillis());
		

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}
}
