package mineplex.core.antihack.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mineplex.anticheat.MineplexAnticheat;
import com.mineplex.anticheat.checks.Check;
import com.mineplex.anticheat.checks.CheckManager;

import mineplex.core.antihack.AntiHack;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class GetVlsCommand extends CommandBase<AntiHack>
{
	public GetVlsCommand(AntiHack plugin)
	{
		super(plugin, AntiHack.Perm.GET_VLS_COMMAND, "getvls");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length > 0)
		{
			Player p = Bukkit.getPlayerExact(args[0]);
			if (p != null)
			{
				CheckManager manager = MineplexAnticheat.getPlugin(MineplexAnticheat.class).getCheckManager();
				for (Check check : manager.getActiveChecks())
				{
					UtilPlayer.message(caller, F.desc(check.getName(), String.valueOf(check.getViolationLevel(p))));
				}
			}
			else
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Could not find player"));
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "No player specified"));
		}
	}
}