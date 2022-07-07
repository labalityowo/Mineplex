package mineplex.game.clans.clans.freeze.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.game.clans.clans.freeze.ClansFreezeManager;

/**
 * Command to enter panic mode
 */
public class PanicCommand extends CommandBase<ClansFreezeManager>
{
	public PanicCommand(ClansFreezeManager plugin)
	{
		super(plugin, ClansFreezeManager.Perm.PANIC_COMMAND, "panic");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{		
		if (Plugin.isPanicking(caller))
		{
			Plugin.unpanic(caller);
		}
		else
		{
			Plugin.panic(caller);
		}
	}
}