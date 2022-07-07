package nautilus.game.arcade.missions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;

public class EnchantItemMissionTracker extends GameMissionTracker<Game>
{

	public EnchantItemMissionTracker(Game game)
	{
		super(MissionTrackerType.GAME_ENCHANT_ITEM, game);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void craftItem(EnchantItemEvent event)
	{
		ItemStack itemStack = event.getItem();

		if (itemStack == null)
		{
			return;
		}

		_manager.incrementProgress(event.getEnchanter(), itemStack.getAmount(), _trackerType, getGameType(), itemStack.getType());
	}
}
