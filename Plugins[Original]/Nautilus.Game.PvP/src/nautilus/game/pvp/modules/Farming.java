package nautilus.game.pvp.modules;

import mineplex.core.itemstack.ItemStackFactory;
import me.chiss.Core.Module.AModule;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Farming extends AModule
{
	public Farming(JavaPlugin plugin) 
	{
		super("Farming", plugin);
	}

	@Override
	public void enable() 
	{

	}

	@Override
	public void disable() 
	{

	}

	@Override
	public void config()
	{

	}

	@Override
	public void commands() 
	{

	}

	@Override
	public void command(Player caller, String cmd, String[] args)
	{

	}
	
	@EventHandler
	public void BlockBreak(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getBlock().getType() != Material.LEAVES)
			return;
		
		if (event.getPlayer().getItemInHand() != null)
			if (event.getPlayer().getItemInHand().getType() == Material.SHEARS)
				return;
		
		if (Math.random() > 0.9)
			event.getBlock().getWorld().dropItemNaturally(
					event.getBlock().getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.APPLE));
		
		if (Math.random() > 0.999)
			event.getBlock().getWorld().dropItemNaturally(
					event.getBlock().getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.GOLDEN_APPLE));
	}

	@EventHandler
	public void BlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;

		if (
				event.getBlock().getTypeId() != 59 &&
				event.getBlock().getTypeId() != 83 &&
				event.getBlock().getTypeId() != 104 &&
				event.getBlock().getTypeId() != 105 &&
				event.getBlock().getTypeId() != 127 &&
				event.getBlock().getTypeId() != 141 &&
				event.getBlock().getTypeId() != 142
				)
			return;

		if (event.getBlock().getLocation().getY() < event.getBlock().getWorld().getSeaLevel() - 12)
		{
			UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot plant " + 
					F.item(ItemStackFactory.Instance.GetName(event.getPlayer().getItemInHand(), true)) + " this deep underground."));
			event.setCancelled(true);
		}
		
		else if (event.getBlock().getLocation().getY() > event.getBlock().getWorld().getSeaLevel() + 24)
		{
			UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot plant " + 
					F.item(ItemStackFactory.Instance.GetName(event.getPlayer().getItemInHand(), true)) + " at this altitude."));
			event.setCancelled(true);
		}
	}
}
