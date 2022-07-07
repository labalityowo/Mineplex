package mineplex.game.clans.tutorial.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.game.clans.tutorial.TutorialManager;

public class FinishCommand extends CommandBase<TutorialManager>
{
	public FinishCommand(TutorialManager plugin)
	{
		super(plugin, TutorialManager.Perm.FINISH_TUTORIAL_COMMAND, "finish", "end");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.finishTutorial(caller);
	}
}