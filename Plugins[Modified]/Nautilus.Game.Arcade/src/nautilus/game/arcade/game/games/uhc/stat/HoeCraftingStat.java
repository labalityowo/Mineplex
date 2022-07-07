package nautilus.game.arcade.game.games.uhc.stat;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;

import nautilus.game.arcade.game.games.uhc.UHC;
import nautilus.game.arcade.stats.StatTracker;

public class HoeCraftingStat extends StatTracker<UHC>
{
		
	public HoeCraftingStat(UHC game)
	{
		super(game);
	}

	@EventHandler
	public void craft(CraftItemEvent event)
	{
		if (event.getCurrentItem().getType() == Material.DIAMOND_HOE)
		{
			getGame().addUHCAchievement((Player) event.getWhoClicked(), "Hoe");
		}
	}
	
}
