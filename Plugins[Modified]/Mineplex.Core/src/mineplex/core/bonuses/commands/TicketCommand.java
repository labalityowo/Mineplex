package mineplex.core.bonuses.commands;

import org.bukkit.entity.Player;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class TicketCommand extends CommandBase<BonusManager>
{
	public TicketCommand(BonusManager plugin)
	{
		super(plugin, BonusManager.Perm.TICKET_COMMAND, "ticket");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Carl", "Missing Args: " + F.elem("/ticket <player> <amount>")));
			return;
		}

		final String targetName = args[0];
		final String ticketString = args[1];
		Player target = UtilPlayer.searchExact(targetName);


		rewardTickets(caller, target, target.getName(), ticketString);
	}

	private void rewardTickets(final Player caller, final Player target, final String targetName, String ticketString)
	{
		try
		{
			final int tickets = Integer.parseInt(ticketString);
			int accountId = Plugin.getClientManager().getAccountId(target);
			Plugin.getRepository().attemptAddTickets(accountId, Plugin.Get(target), tickets, new Callback<Boolean>()
			{
				@Override
				public void run(Boolean data)
				{
					if (data)
					{
						UtilPlayer.message(caller, F.main("Carl", "You gave " + F.elem(tickets + " Carl Tickets") + " to " + F.name(targetName) + "."));

						if (target != null && !target.equals(caller))
						{
							UtilPlayer.message(target, F.main("Carl", F.name(caller.getName()) + " gave you " + F.elem(tickets + " Carl Tickets") + "."));
						}
					}
					else
					{
						UtilPlayer.message(caller, F.main("Carl", "Failed to give tickets. Try again later!"));
					}
				}
			});
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main("Carl", "Invalid Ticket Amount"));
		}
	}
}