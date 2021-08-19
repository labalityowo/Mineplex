package mineplex.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.UtilItem;

public class FoodDupeFix extends MiniPlugin
{
	public FoodDupeFix(JavaPlugin plugin)
	{
		super("Food Dupe Fix", plugin);
	}

	// Use Lowest priority so we get called first event.getItem isn't changed
	@EventHandler(priority = EventPriority.LOWEST)
	public void fixFoodDupe(PlayerItemConsumeEvent event)
	{
		if (UtilItem.isFood(event.getItem()))
		{
			if (!event.getItem().equals(event.getPlayer().getItemInHand()))
			{
				event.setCancelled(true);
			}
		}
	}
}
