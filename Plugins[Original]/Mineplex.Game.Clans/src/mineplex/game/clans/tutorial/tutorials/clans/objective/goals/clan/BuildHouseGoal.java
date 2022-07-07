package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;

public class BuildHouseGoal extends ObjectiveGoal<ClanObjective>
{
	private List<ItemStack> _items = Lists.newArrayList(
									new ItemStack(Material.SMOOTH_BRICK, 54),
									new ItemStack(Material.TORCH, 2),
									new ItemStack(Material.IRON_DOOR, 1)
								);


	public BuildHouseGoal(ClanObjective objective)
	{
		super(
				objective,
				"Build a House",
				"Build a House (place all your blocks)",
				"The first thing you should do on your land is build a house, even " +
						"if it’s made of dirt! This will give you a safe place to store your loot!",
				DyeColor.ORANGE
		);
	}

	@Override
	protected void customStart(Player player)
	{
		_items.forEach(item -> {
			player.getInventory().addItem(item);
		});
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (!getObjective().getPlugin().isInTutorial(event.getPlayer()))
		{
			return;
		}

		event.setCancelled(false);


		if (getObjective().getPlugin().isInBuildArea(event.getPlayer(), event.getBlock()))
		{
			// Run 1 tick later because inventory doesn't get updated instantly
			ClansManager.getInstance().runSync(() -> {
				boolean ja = true;
				for (ItemStack stack : event.getPlayer().getInventory().getContents())
				{
					if (stack == null)
						continue;

					for (ItemStack other : _items)
						if (stack.getType() == other.getType())
						{
							ja = false;
							break;
						}
				}

				if (ja) // JA!
					finish(event.getPlayer());
			});
		}
		else
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You are not allowed to place blocks here."));
			event.setCancelled(true);
		}
	}
}
