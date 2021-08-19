package mineplex.core.boosters.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.boosters.Booster;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.boosters.tips.BoosterThankManager;
import mineplex.core.boosters.tips.TipAddResult;
import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

/**
 * @author Shaun Bennett
 */
public class ThankCommand extends CommandBase<BoosterManager>
{
	public ThankCommand(BoosterManager plugin)
	{
		super(plugin, BoosterManager.Perm.THANK_COMMAND, "thank");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			// try to thank the current amplifier group
			String boosterGroup = Plugin.getBoosterGroup();
			if (boosterGroup == null || boosterGroup.length() <= 0)
			{
				UtilPlayer.message(caller, F.main("Amplifier", "You must specify an Amplifier Group"));
				return;
			}

			attemptToTipGroup(caller, boosterGroup);
		}
		else
		{
			String boosterGroup = StringUtils.join(args, ' ');
			attemptToTipGroup(caller, boosterGroup);
		}
	}

	private void attemptToTipGroup(Player caller, String boosterGroup)
	{
		Booster booster = Plugin.getActiveBooster(boosterGroup);
		if (booster == null)
		{
			// Give a friendly oops message
			UtilPlayer.message(caller, F.main("Amplifier", "There was an error handling your request. Try again later"));
			return;
		}
		else
		{
			Plugin.getBoosterThankManager().addTip(caller, booster, result ->
			{
				if (result == TipAddResult.SUCCESS)
				{
					UtilPlayer.message(caller, F.main("Tip", "You thanked " + F.name(booster.getPlayerName()) + ". They earned " + F.currency(GlobalCurrency.TREASURE_SHARD, BoosterThankManager.TIP_FOR_SPONSOR) + " and you got "
							+ F.currency(GlobalCurrency.TREASURE_SHARD, BoosterThankManager.TIP_FOR_TIPPER)) + " in return!");
					caller.playSound(caller.getLocation(), Sound.LEVEL_UP, 1f, 1f);
				}
				else if (result.getFriendlyMessage() != null)
				{
					UtilPlayer.message(caller, F.main("Amplifier", result.getFriendlyMessage()));
				}
			});
		}
	}
}