package nautilus.game.arcade.game.games.alieninvasion.kit;

import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.alieninvasion.PhaserProjectile;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkBlaster extends Perk
{

	public PerkBlaster()
	{
		super("Space Blaster", new String[0]);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (event.getItem() == null || event.getItem().getType() != Material.DIAMOND_BARDING)
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), 200, false, false))
		{
			return;
		}

		new PhaserProjectile(Manager, player);
	}
}
