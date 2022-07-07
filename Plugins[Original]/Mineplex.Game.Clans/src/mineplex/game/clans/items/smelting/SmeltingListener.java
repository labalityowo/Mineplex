package mineplex.game.clans.items.smelting;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for smelting related events triggered by players to carry out
 * item smelting for base resources and ores.
 * @author MrTwiggy
 *
 */
public class SmeltingListener implements Listener
{

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().isSneaking())
		{
			Block clicked = event.getClickedBlock();
			
			if (clicked.getType() == Material.FURNACE)
			{
				Smelter.smeltItemInHand(event.getPlayer());
			}
		}
	}
}
