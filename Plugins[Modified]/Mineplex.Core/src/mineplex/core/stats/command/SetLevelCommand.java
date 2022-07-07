package mineplex.core.stats.command;

import org.bukkit.entity.Player;

import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.stats.StatsManager;

public class SetLevelCommand extends CommandBase<StatsManager>
{
	public SetLevelCommand(StatsManager plugin)
	{
		super(plugin, StatsManager.Perm.SET_LEVEL_COMMAND, "setlevel");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length != 2)
		{
			UtilPlayer.message(caller, F.main("Stats", "/setlevel [player] [level]"));
			return;
		}

		Player target = UtilPlayer.searchOnline(caller, args[0], true);

		if (target == null)
		{
			return;
		}

		int level;

		try
		{
			level = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException ex)
		{
			UtilPlayer.message(caller, F.main("Stats", F.elem(args[1]) + " is not a number"));
			return;
		}

		if (level < 0 || level > LevelingManager.getMaxLevel())
		{
			UtilPlayer.message(caller, F.main("Stats", "That level is invalid"));
			return;
		}

		long amountNeeded = 0;

		for (int i = 0; i < level; i++)
		{
			amountNeeded += Achievement.GLOBAL_MINEPLEX_LEVEL.getLevels()[i];
		}
		
		long amountHas = Plugin.Get(target).getStat(Achievement.GLOBAL_MINEPLEX_LEVEL.getStats()[0]);
		
		if (amountNeeded - amountHas <= 0)
		{
			UtilPlayer.message(caller, F.main("Stats", "That target already has level " + F.elem(level) + " or higher!"));
			return;
		}
		
		Plugin.incrementStat(target, Achievement.GLOBAL_MINEPLEX_LEVEL.getStats()[0], amountNeeded - amountHas);
		UtilPlayer.message(caller, F.main("Stats", "Updated " + F.elem(target.getName()) + "'s level to " + F.elem(level)));
	}
}