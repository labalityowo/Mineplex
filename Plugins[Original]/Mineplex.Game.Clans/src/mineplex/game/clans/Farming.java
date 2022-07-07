package mineplex.game.clans;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilItem;
import mineplex.core.itemstack.ItemStackFactory;

public class Farming extends MiniPlugin
{
	public Farming(JavaPlugin plugin) 
	{
		super("Farming", plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void BlockBreak(BlockBreakEvent event)
	{
		if (event.getBlock().getType() != Material.LEAVES)
			return;
		
		if (UtilItem.matchesMaterial(event.getPlayer().getItemInHand(), Material.SHEARS))
			return;

		Location dropLocation = event.getBlock().getLocation().add(0.5, 0.5, 0.5);

		if (Math.random() > 0.9)
			event.getBlock().getWorld().dropItemNaturally(dropLocation, ItemStackFactory.Instance.CreateStack(Material.APPLE));
		
		if (Math.random() > 0.999)
			event.getBlock().getWorld().dropItemNaturally(dropLocation, ItemStackFactory.Instance.CreateStack(Material.GOLDEN_APPLE));
	}
}