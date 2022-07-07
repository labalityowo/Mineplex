package mineplex.game.clans.economy;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class SetGoldCommand extends CommandBase<GoldManager>
{
	public SetGoldCommand(GoldManager plugin)
	{
		super(plugin, GoldManager.Perm.SET_GOLD_COMMAND, "setgold");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null || args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "Error! Usage: " + F.elem("/setgold <player> <amount>")));
			return;
		}

		final String targetName = args[0];
		final String goldString = args[1];
		Player target = UtilPlayer.searchExact(targetName);

		try
		{
			if (target == null)
			{
				Plugin.getClientManager().loadClientByName(targetName, client ->
				{
					if (client != null)
					{
						setGold(caller, null, targetName, client.getAccountId(), Integer.parseInt(goldString));
					}
					else
					{
						UtilPlayer.message(caller, F.main("Gold", "Could not find player " + F.name(targetName)));
					}
				});
			}
			else
			{
				setGold(caller, target, target.getName(), Plugin.getClientManager().Get(target).getAccountId(), Integer.parseInt(goldString));
			}
		}
		catch (Exception e)
		{
			UtilPlayer.message(target, F.main("Clans", "You must provide a valid number in the gold parameter."));
		}
	}

	private void setGold(final Player caller, final Player target, final String targetName, final int accountId, final int gold)
	{
		Plugin.setGold(new Callback<Boolean>()
		{
			public void run(Boolean completed)
			{
				UtilPlayer.message(caller, F.main("Gold", "You set " + F.name(targetName) + "'s Gold to " + F.elem(gold) + "."));
			}
		}, caller.getName(), targetName, accountId, gold, true);
	}
}