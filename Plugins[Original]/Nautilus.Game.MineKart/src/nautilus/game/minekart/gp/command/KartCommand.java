package nautilus.game.minekart.gp.command;

import org.bukkit.entity.Player;

import nautilus.game.minekart.gp.GPManager;
import nautilus.game.minekart.kart.KartType;
import mineplex.core.command.CommandBase;
import mineplex.core.common.Rank;

public class KartCommand extends CommandBase<GPManager>
{
	public KartCommand(GPManager plugin)
	{
		super(plugin, Rank.ADMIN, "kart");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
			return;

		for (KartType type : KartType.values())
		{
			if (args[0].equalsIgnoreCase(type.GetName()))
			{
				Plugin.SelectKart(caller, type);
			}
		}
	}
}
