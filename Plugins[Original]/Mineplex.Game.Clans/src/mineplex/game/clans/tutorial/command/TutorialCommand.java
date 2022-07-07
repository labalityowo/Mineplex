package mineplex.game.clans.tutorial.command;

import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.tutorial.TutorialManager;

public class TutorialCommand extends MultiCommandBase<TutorialManager>
{
	public TutorialCommand(TutorialManager plugin)
	{
		super(plugin, TutorialManager.Perm.TUTORIAL_COMMAND, "tutorial", "tut");

		AddCommand(new StartCommand(plugin));
		AddCommand(new FinishCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.main("Tutorial", "/tutorial start <name>"));
	}
}