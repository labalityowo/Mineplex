package mineplex.core.thank.command;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.thank.ThankManager;
import mineplex.core.thank.ThankResult;

/**
 * @author Shaun Bennett
 */
public class ThankCommand extends CommandBase<ThankManager>
{
	public ThankCommand(ThankManager plugin)
	{
		super(plugin, ThankManager.Perm.THANK_COMMAND, "thank");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length != 1)
		{
			UtilPlayer.message(caller, F.main("Thank", "Usage: " + F.elem("/thank <player>")));
			return;
		}

		String playerName = args[0];
		Player player = UtilPlayer.searchOnline(caller, playerName, true);
		if (player != null)
		{
			Plugin.thankPlayer(player, caller, ThankManager.DEFAULT_RECEIVER_REWARD, ThankManager.DEFAULT_SENDER_REWARD,
					"ThankCommand", false, result ->
					{
						String message;

						switch (result)
						{
							case SUCCESS:
								message = "You thanked " + F.name(player.getName()) + " and earned " + F.currency(GlobalCurrency.TREASURE_SHARD, ThankManager.DEFAULT_SENDER_REWARD) + "!";
								break;
							case CANNOT_THANK_SELF:
								message = "You cannot thank yourself!";
								break;
							case COOLDOWN_DATABASE:
								message = "You can only /thank once per day!";
								break;
							case COOLDOWN_RECHARGE:
								message = null;
								break;
							default:
								message = "An error occurred. Try again later";
						}

						if (message != null)
						{
							UtilPlayer.message(caller, F.main("Thank", message));
						}

						if (result == ThankResult.SUCCESS && player.isOnline())
						{
							UtilPlayer.message(player, F.main("Thank", F.name(caller.getName()) + " used " + F.elem("/thank") + " on you! +" + F.currency(GlobalCurrency.TREASURE_SHARD, ThankManager.DEFAULT_RECEIVER_REWARD) + "!"));
							UtilPlayer.message(player, F.main("Thank", "You can claim your reward at " + F.name("Carl the Creeper")));
							player.playSound(player.getEyeLocation(), Sound.LEVEL_UP, 1f, 1.2f);
						}
					});
		}
	}
}