package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.finalobj;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.delayedtask.DelayedTask;
import mineplex.core.delayedtask.DelayedTaskClient;
import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.tutorial.objective.Objective;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.tutorial.tutorials.clans.objective.FinalObjective;

public class TpClanHomeGoal extends ObjectiveGoal<FinalObjective>
{
	public TpClanHomeGoal(FinalObjective objective)
	{
		super(
				objective,
				"Teleport to Clan Home",
				"Type '/c home' to teleport to Clan Home",
				"You can teleport back to your Clan Home at any time! If enemies break your bed, then you cannot teleport to it!",
				null
		);

//		setStartMessageDelay(120);
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
	public void teleport(ClansCommandPreExecutedEvent event)
	{
		if (event.getArguments().length != 1 || !event.getArguments()[0].equalsIgnoreCase("home"))
		{
			return;
		}
		
		if (!contains(event.getPlayer()))
		{
			return;
		}

		DelayedTask.Instance.doDelay(
				event.getPlayer(),
				"Tutorial Home Teleport",
				new Callback<DelayedTaskClient>()
				{
					@Override
					public void run(DelayedTaskClient data)
					{
						UtilPlayer.message(event.getPlayer(), F.main("Clans", "You have teleported to your Clan's Home."));
						event.getPlayer().teleport(getObjective().getPlugin().getTutorialSession(event.getPlayer()).getHomeLocation());
						finish(event.getPlayer());
					}
				},
				new Callback<DelayedTaskClient>()
				{
					@Override
					public void run(DelayedTaskClient data)
					{
						UtilTextMiddle.display("", "Teleporting to Clan Home in " + F.time(UtilTime.MakeStr(Math.max(0, data.getTimeLeft("Tutorial Home Teleport")))), 0, 5, 0, data.getPlayer());
					}
				},
				new Callback<DelayedTaskClient>()
				{
					@Override
					public void run(DelayedTaskClient data)
					{
						UtilPlayer.message(data.getPlayer(), F.main("Clans", "Teleport has been cancelled due to movement."));
					}
				},
				15 * 1000, // 15 second cooldown
				false
		);

		event.setCancelled(true);
	}
}
