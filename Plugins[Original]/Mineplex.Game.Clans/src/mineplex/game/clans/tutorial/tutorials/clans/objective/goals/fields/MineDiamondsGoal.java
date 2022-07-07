package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.fields;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.FieldsObjective;

public class MineDiamondsGoal extends ObjectiveGoal<FieldsObjective>
{
	private HashMap<UUID, AtomicInteger> _playersMap;

	public MineDiamondsGoal(FieldsObjective objective)
	{
		super(
				objective,
				"Mine Diamonds",
				"Mine Diamonds in the Fields",
				"Mining in the Fields is a great way to make lots of money! The ores will " +
						"regenerate over time. Be careful of enemies though!",
				DyeColor.LIME
		);

		_playersMap = new HashMap<>();
	}

	@Override
	protected void customStart(Player player)
	{
		player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));

		_playersMap.put(player.getUniqueId(), new AtomicInteger(0));
	}

	@Override
	protected void customFinish(Player player)
	{
		_playersMap.remove(player.getUniqueId());
	}
	
	@Override
	protected void customLeave(Player player)
	{
		_playersMap.remove(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!contains(event.getPlayer()) || event.getBlock().getType() != Material.DIAMOND_ORE)
			return;

		TutorialRegion region = getObjective().getPlugin().getRegion(event.getPlayer());
		if (getObjective().getPlugin().isIn(event.getBlock().getLocation(), region, ClansMainTutorial.Bounds.FIELDS))
		{
			event.setCancelled(true);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 1.5, 0.5), new ItemStack(Material.DIAMOND));
			event.getBlock().setType(Material.COBBLESTONE);

			Bukkit.getServer().getScheduler().runTaskLater(getObjective().getJavaPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					if (contains(event.getPlayer()))
					{
						event.getBlock().setType(Material.DIAMOND_ORE);
					}
				}
			}, 20 * 10);
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event)
	{
		if (!contains(event.getPlayer()))
			return;

		if (event.getItem().getItemStack().getType() == Material.DIAMOND)
		{
			if(_playersMap.get(event.getPlayer().getUniqueId()) == null) return;
			int count = _playersMap.get(event.getPlayer().getUniqueId()).incrementAndGet();
			if (count == 10)
				finish(event.getPlayer());
		}
	}

	@Override
	public String getDescription(Player player)
	{
		AtomicInteger count = _playersMap.get(player.getUniqueId());
		if (count == null)
			return "Search for some diamonds in the Fields and mine them";
		else
			return "Mine Diamonds " + count.get() + "/10";
	}
}
