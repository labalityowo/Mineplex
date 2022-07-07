package mineplex.core.antihack.commands;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.animations.BanwaveAnimationSpin;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class TestBanCommand extends CommandBase<AntiHack>
{
	public TestBanCommand(AntiHack plugin)
	{
		super(plugin, AntiHack.Perm.TEST_BAN_COMMAND, "testban");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length > 0)
		{
			Player p = Bukkit.getPlayerExact(args[0]);
			if (p != null)
			{
				new BanwaveAnimationSpin().run(p, () ->
				{
					String reason = C.cRed + C.Bold + "You are banned for permanent by Test" +
							"\n" + C.cWhite + "Seems to be speeding up time. (" + ThreadLocalRandom.current().nextInt(200, 400) + " packets/150 ms)" +
							"\n" + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.mineplex.com/appeals";
					p.kickPlayer(reason);

					Plugin.announceBan(p);
				});
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