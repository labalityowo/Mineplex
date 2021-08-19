package mineplex.hub.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.hub.modules.EasterEggHunt;

public class EggAddCommand extends CommandBase<EasterEggHunt>
{
	public EggAddCommand(EasterEggHunt plugin)
	{
		super(plugin, EasterEggHunt.Perm.ADD_EGG_COMMAND, "addegg");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: /addegg <yyyy-[m]m-[d]d>"));
			return;
		}
		Plugin.addEgg(caller, args[0]);
	}
}