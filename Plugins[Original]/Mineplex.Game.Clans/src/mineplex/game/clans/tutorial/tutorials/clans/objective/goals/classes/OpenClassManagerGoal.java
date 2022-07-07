package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.classes;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClassesObjective;

public class OpenClassManagerGoal extends ObjectiveGoal<ClassesObjective>
{
	public OpenClassManagerGoal(ClassesObjective objective)
	{
		super(
				objective, "Open Class Manager",
				"Right-Click on the Enchantment Table",
				"Each class has lots of different skills, and you can pick which ones you want to " +
						"equip! Right-Click on an " + F.elem("Enchanting Table") + " to have a look at " +
						"this menu.",
				DyeColor.CYAN
		);
	}

	@Override
	protected void customStart(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!contains(event.getPlayer()))
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}
		
		if (!event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE))
		{
			return;
		}
		
		finish(event.getPlayer());
	}
}
