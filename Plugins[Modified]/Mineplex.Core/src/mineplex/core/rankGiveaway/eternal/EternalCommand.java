package mineplex.core.rankGiveaway.eternal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class EternalCommand extends CommandBase<EternalGiveawayManager>
{
	public EternalCommand(EternalGiveawayManager plugin)
	{
		super(plugin, EternalGiveawayManager.Perm.ETERNAL_COMMAND, "eternaltest");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Plugin.attemptToGiveEternal(caller, () ->
			{
				Location location = caller.getLocation().add(0.5, 0.5, 0.5);
				new EternalGiveawayAnimation(Plugin, location, 3000L);
			});
		}
		else
		{
			String target = args[1];
			Player player = Bukkit.getPlayer(target);
			if (player == null)
			{
				UtilPlayer.message(caller, F.main("Eternal", "That player is not online"));
				return;
			}
			Plugin.attemptToGiveEternal(player, () ->
			{
				Location location = caller.getLocation().add(0.5, 0.5, 0.5);
				new EternalGiveawayAnimation(Plugin, location, 3000L);
			});
		}
	}
}