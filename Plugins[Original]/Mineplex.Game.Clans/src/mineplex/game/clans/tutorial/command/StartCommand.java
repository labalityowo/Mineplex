package mineplex.game.clans.tutorial.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.tutorial.TutorialManager;
import mineplex.game.clans.tutorial.TutorialType;

public class StartCommand extends CommandBase<TutorialManager>
{
	public StartCommand(TutorialManager plugin)
	{
		super(plugin, TutorialManager.Perm.START_TUTORIAL_COMMAND, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length != 1)
		{
			UtilPlayer.message(caller, F.main("Tutorial", "/tutorial start <name>"));
			return;
		}

		TutorialType type = null;

		for (TutorialType check : TutorialType.values())
		{
			if (check.name().equalsIgnoreCase(args[0]))
			{
				type = check;
			}
		}

		if (type != null)
		{
			Plugin.openTutorialMenu(caller, type);
		}
		else
		{
			UtilPlayer.message(caller, F.main("Tutorial", "Invalid Tutorial " + F.elem(args[0])));
		}
	}
}