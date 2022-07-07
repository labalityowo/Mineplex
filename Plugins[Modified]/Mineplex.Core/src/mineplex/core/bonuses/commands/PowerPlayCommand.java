package mineplex.core.bonuses.commands;

import java.time.LocalDate;
import java.time.YearMonth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.powerplayclub.PowerPlayData;

public class PowerPlayCommand extends CommandBase<BonusManager>
{
	private BonusManager _bonusManager;

	public PowerPlayCommand(BonusManager manager)
	{
		super(manager, BonusManager.Perm.POWER_PLAY_COMMAND, "powerplay");
		_bonusManager = manager;
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.main("Power Play Club", "Missing Args: " + F.elem("/powerplay <player>")));
			return;
		}

		Player player = Bukkit.getPlayer(args[0]);
		if (player == null) {
			caller.sendMessage(ChatColor.RED + "Couldn't find player");
			return;
		}

		PowerPlayData cached = _bonusManager.getPowerPlayClubRepository().getCachedData(player);
		cached.getUnclaimedMonths().add(YearMonth.now());
		_bonusManager.getPowerPlayClubRepository().addSubscription(_bonusManager.getClientManager().Get(player).getAccountId(), LocalDate.now(), "month");

		caller.sendMessage(ChatColor.GREEN + "Gave a month's subscription to " + player.getName());
	}
}