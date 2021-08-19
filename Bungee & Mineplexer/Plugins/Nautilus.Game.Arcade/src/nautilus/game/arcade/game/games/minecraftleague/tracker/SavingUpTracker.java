package nautilus.game.arcade.game.games.minecraftleague.tracker;

import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;

public class SavingUpTracker extends StatTracker<MinecraftLeague>
{
	public SavingUpTracker(MinecraftLeague game)
	{
		super(game);
	}

	@EventHandler
	public void build(CraftItemEvent e)
	{
		if (e.getRecipe().getResult().getType() == Material.DIAMOND_CHESTPLATE)
			addStat((Player)e.getWhoClicked(), "SavingUp", 1, false, false);
	}

}