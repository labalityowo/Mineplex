package mineplex.core.punish.Command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.punish.Punish;
import mineplex.core.punish.UI.PunishShop;
import mineplex.core.punish.UI.history.PunishHistoryPage;

public class HistoryCommand extends CommandBase<Punish>
{
	private PunishShop _punishShop;

	public HistoryCommand(Punish plugin)
	{
		super(plugin, Punish.Perm.PUNISHMENT_HISTORY_COMMAND, "history", "phistory", "ph");

		_punishShop = new PunishShop(plugin);
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Player target;
		if (args.length < 1 || !Plugin.GetClients().Get(caller).hasPermission(Punish.Perm.PUNISHMENT_COMMAND))
		{
			target = caller;
		}
		else
		{
			target = Bukkit.getPlayerExact(args[0]);
		}

		if (target != null)
		{
			Plugin.GetRepository().LoadPunishClient(caller.getName(), clientToken ->
			{
				Plugin.LoadClient(clientToken);
				_punishShop.openPageForPlayer(caller, new PunishHistoryPage(Plugin, _punishShop, caller, target.getName(), false, null, null));
			});

			return;
		}

		final String playerName = args[0];

		Plugin.GetRepository().MatchPlayerName(matches ->
		{
			boolean matchedExact = false;

			for (String match : matches)
			{
				if (match.equalsIgnoreCase(playerName))
				{
					matchedExact = true;
				}
			}

			if (matchedExact)
			{
				matches.removeIf(s -> !s.equalsIgnoreCase(playerName));
			}

			UtilPlayer.searchOffline(matches, t ->
			{
				if (t == null)
				{
					return;
				}

				Plugin.GetRepository().LoadPunishClient(t, clientToken ->
				{
					Plugin.LoadClient(clientToken);
					_punishShop.openPageForPlayer(caller, new PunishHistoryPage(Plugin, _punishShop, caller, playerName, false, null, null));
				});
			}, caller, playerName, true);
		}, playerName);
	}
}
