package mineplex.gemhunters.moderation.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.gemhunters.moderation.ModerationModule;

public class ModeratorModeCommand extends CommandBase<ModerationModule>
{
	public ModeratorModeCommand(ModerationModule plugin)
	{
		super(plugin, ModerationModule.Perm.MODERATOR_MODE_COMMAND, "modmode", "staffmode", "mm", "o");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (Plugin.isModerating(caller))
		{
			Plugin.disableModeratorMode(caller);
		}
		else
		{
			Plugin.enableModeratorMode(caller);
		}
	}	
}