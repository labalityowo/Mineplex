package mineplex.game.clans.economy;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class GoldCommand extends CommandBase<GoldManager>
{
	public GoldCommand(GoldManager plugin)
	{
		super(plugin, GoldManager.Perm.GIVE_GOLD_COMMAND, "givegold");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Gold", "Missing Args: " + F.elem("/givegold <player> <amount>")));
			return;
		}

		final String targetName = args[0];
		final String goldString = args[1];
		Player target = UtilPlayer.searchExact(targetName);

		if (target == null)
		{
			Plugin.getClientManager().loadClientByName(targetName, client ->
			{
				if (client != null)
				{
					rewardGold(caller, null, targetName, client.getAccountId(), goldString);
				}
				else
				{
					UtilPlayer.message(caller, F.main("Gold", "Could not find player " + F.name(targetName)));
				}
			});
		}
		else
		{
			rewardGold(caller, target, target.getName(), Plugin.getClientManager().Get(target).getAccountId(), goldString);
		}
	}

	private void rewardGold(final Player caller, final Player target, final String targetName, final int accountId, String goldString)
	{
		try
		{
			int gold = Integer.parseInt(goldString);
			rewardGold(caller, target, targetName, accountId, gold);
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main("Gold", "Invalid Gold Amount"));
		}
	}

	private void rewardGold(final Player caller, final Player target, final String targetName, final int accountId, final int gold)
	{
		Plugin.rewardGold(success ->
		{
			if (!success)
			{
				UtilPlayer.message(caller, F.main("Gold", "You cannot deduct that much " + F.elem("Gold") + " from " + F.name(targetName) + "."));
				return;
			}
			UtilPlayer.message(caller, F.main("Gold", "You gave " + F.elem(gold + " Gold") + " to " + F.name(targetName) + "."));
			
			if (target != null)
			{
				UtilPlayer.message(target, F.main("Gold", F.name(caller.getName()) + " gave you " + F.elem(gold + " Gold") + "."));
			}
		}, accountId, targetName, gold, true);
	}
}