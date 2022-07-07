package mineplex.core.punish.Command;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.punish.Punish;
import mineplex.core.punish.UI.staff.PunishStaffPage;
import mineplex.core.punish.UI.PunishShop;

public class PunishCommand extends CommandBase<Punish>
{
	private PunishShop _punishShop;

	public PunishCommand(Punish plugin)
	{
		super(plugin, Punish.Perm.PUNISHMENT_COMMAND, "punish", "p");

		_punishShop = new PunishShop(plugin);
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null || args.length < 2)
		{
			Plugin.Help(caller);
		}
		else
		{
			final String playerName = args[0];
			String reason = args[1];
			
			for (int i = 2; i < args.length; i++)
			{
				reason += " " + args[i];
			}
			
			final String finalReason = reason;
			
			//Match exact online first
			Player target = Bukkit.getPlayerExact(playerName);

			if (target != null)
			{
				AtomicBoolean wasDisguised = new AtomicBoolean(false);
				AtomicReference<String> originalName = new AtomicReference<>(playerName);
				AtomicReference<String> disguisedName = new AtomicReference<>(playerName);

				CoreClient client = Plugin.GetClients().Get(target.getUniqueId());
				if (!client.getName().equals(target.getName()))
				{
					// Has to be the only possibility, but....
					if (client.isDisguised() && client.getDisguisedAs().equalsIgnoreCase(playerName))
					{
						originalName.set(client.getName());
						disguisedName.set(client.getDisguisedAs());
						wasDisguised.set(true);
					}
					else
					{
						UtilPlayer.message(caller, F.main("Punish", "An unexpected error occured. Please contact a developer immediately"));
						return;
					}
				}

				Plugin.GetRepository().LoadPunishClient(originalName.get(), clientToken ->
				{
					Plugin.LoadClient(clientToken);
					_punishShop.openPageForPlayer(caller, new PunishStaffPage(Plugin, _punishShop, caller, originalName.get(), finalReason, wasDisguised.get(), originalName.get(), disguisedName.get()));
				});
				
				return;
			}

			String finalPlayerName = playerName;
			
			//Check repo
			Plugin.GetRepository().MatchPlayerName(matches ->
			{
				boolean matchedExact = false;

				for (String match : matches)
				{
					if (match.equalsIgnoreCase(finalPlayerName))
					{
						matchedExact = true;
					}
				}

				if (matchedExact)
				{
					for (Iterator<String> matchIterator = matches.iterator(); matchIterator.hasNext();)
					{
						if (!matchIterator.next().equalsIgnoreCase(finalPlayerName))
						{
							matchIterator.remove();
						}
					}
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
						_punishShop.openPageForPlayer(caller, new PunishStaffPage(Plugin, _punishShop, caller, t, finalReason, false, null, null));
					});
				}, caller, finalPlayerName, true);
			}, playerName);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		return tabCompletePlayerNames(sender, args);
	}
}