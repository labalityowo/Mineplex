package mineplex.core.antihack.commands;

import org.bukkit.entity.Player;

import mineplex.core.antihack.AntiHack;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class AnticheatOnCommand extends CommandBase<AntiHack>
{
	public AnticheatOnCommand(AntiHack plugin)
	{
		super(plugin, AntiHack.Perm.ANTICHEAT_TOGGLE_COMMAND, "acon");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.enableAnticheat();
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Enabled anticheat"));
	}
}