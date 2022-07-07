package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan;

import java.util.HashSet;
import java.util.UUID;

import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;

public class LeaveSpawnGoal extends ObjectiveGoal<ClanObjective>
{
	public LeaveSpawnGoal(ClanObjective objective)
	{
		super(
				objective,
				"Leave Spawn Island",
				"Jump off Spawn Island",
				F.elem("Spawn Island") + " is where you will respawn when you die. This area is " +
						"a " + F.elem("Safe Zone") + ", meaning that players cannot hurt each other. " +
						"From here, you can teleport to various places, as well as read some helpful " +
						"hints.",
				DyeColor.WHITE
		);

		// 2 seconds after start message
		setStartMessageDelay(20 * 11);
	}

	@Override
	protected void setup(Player player, TutorialRegion region)
	{

	}

	@Override
	protected void customStart(Player player)
	{
		player.getInventory().clear();
	}

	@Override
	protected void customFinish(Player player)
	{

	}

	@EventHandler
	public void onCommand(ClansCommandPreExecutedEvent event)
	{
		if(contains(event.getPlayer())) event.setCancelled(true);
	}

	@EventHandler
	public void checkRegion(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (UUID uuid : getActivePlayers())
		{
			Player player = Bukkit.getPlayer(uuid);
			if(player == null || !player.isOnline()) continue;
 			if (!getObjective().getPlugin().isIn(player, ClansMainTutorial.Bounds.SPAWN))
			{
				finish(Bukkit.getPlayer(uuid));
			}
		}
	}
}
