package nautilus.game.arcade.missions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;

public class CraftItemMissionTracker extends GameMissionTracker<Game>
{

	public CraftItemMissionTracker(Game game)
	{
		super(MissionTrackerType.GAME_CRAFT_ITEM, game);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void craftItem(CraftItemEvent event)
	{
		ItemStack itemStack = event.getRecipe().getResult();

		if (itemStack == null)
		{
			return;
		}

		_manager.incrementProgress((Player) event.getWhoClicked(), itemStack.getAmount(), _trackerType, getGameType(), itemStack.getType());
	}
}
