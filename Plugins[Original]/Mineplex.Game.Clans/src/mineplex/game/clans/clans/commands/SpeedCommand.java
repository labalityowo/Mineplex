package mineplex.game.clans.clans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;

public class SpeedCommand extends CommandBase<ClansManager>
{
	public SpeedCommand(ClansManager plugin)
	{
		super(plugin, ClansManager.Perm.SPEED_COMMAND, "speed");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.help("/speed <player> <walk|fly> <speed>", "Set a player's walk/fly speed to an amount", ChatColor.GOLD));
			return;
		}
		Player player = Bukkit.getPlayer(args[0]);
		if (player == null)
		{
			UtilPlayer.message(caller, F.main("Speed", "That player is not online!"));
			return;
		}
		if (args[1].equalsIgnoreCase("walk"))
		{
			float amount;
			try
			{
				amount = Float.parseFloat(args[2]);
			}
			catch (NumberFormatException ex)
			{
				UtilPlayer.message(caller, F.main("Speed", "That is not a number!"));
				return;
			}
			String error = validateSpeed(amount);
			if (error == null)
			{
				player.setWalkSpeed(amount);
				UtilPlayer.message(caller, F.main("Speed", "Set " + player.getName() + "'s walk speed to " + amount));
				return;
			}
			else
			{
				UtilPlayer.message(caller, F.main("Speed", error));
				return;
			}
		}
		else if (args[1].equalsIgnoreCase("fly"))
		{
			float amount;
			try
			{
				amount = Float.parseFloat(args[2]);
			}
			catch (NumberFormatException ex)
			{
				UtilPlayer.message(caller, F.main("Speed", "That is not a number!"));
				return;
			}
			String error = validateSpeed(amount);
			if (error == null)
			{
				player.setFlySpeed(amount);
				UtilPlayer.message(caller, F.main("Speed", "Set " + player.getName() + "'s fly speed to " + amount));
				return;
			}
			else
			{
				UtilPlayer.message(caller, F.main("Speed", error));
				return;
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main("Speed","That is not a valid speed type!"));
			return;
		}
	}

	private String validateSpeed(float value)
	{
		if (value < 0.0F)
		{
			if (value < -1.0F)
			{
				return value + " is too low";
			}
		}
		else if(value > 1.0F)
		{
			return value + " is too high";
		}
		return null;
	}
}