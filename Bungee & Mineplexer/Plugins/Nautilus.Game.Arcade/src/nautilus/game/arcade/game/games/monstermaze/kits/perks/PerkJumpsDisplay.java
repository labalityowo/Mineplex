package nautilus.game.arcade.game.games.monstermaze.kits.perks;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PerkJumpsDisplay extends Perk
{
	public PerkJumpsDisplay()
	{
		super("Display", new String[0], false);
	}

	@EventHandler
	public void onShow(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			if (player.getInventory().getItem(8) == null || player.getInventory().getItem(8).getAmount() <= 0)
			{
				UtilTextBottom.display(C.cWhite + "No jumps left", player);
			}
			else
			{
				UtilTextBottom.display(F.elem(player.getInventory().getItem(8).getAmount() + "") + C.cWhite + " jumps left", player);
			}
		}
	}
}
