package mineplex.core.antihack.commands;

import org.bukkit.entity.Player;

import mineplex.core.antihack.AntiHack;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class AnticheatOffCommand extends CommandBase<AntiHack>
{
	public AnticheatOffCommand(AntiHack plugin)
	{
		super(plugin, AntiHack.Perm.ANTICHEAT_TOGGLE_COMMAND, "acoff");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.disableAnticheat();
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Disabled anticheat"));
	}
}