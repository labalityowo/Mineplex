package mineplex.core.teleport.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.core.teleport.Teleport;

public class TeleportCommand extends MultiCommandBase<Teleport>
{
	public TeleportCommand(Teleport plugin)
	{
		super(plugin, Teleport.Perm.TELEPORT_COMMAND, "tp", "teleport");
		 
		AddCommand(new AllCommand(plugin));
		AddCommand(new BackCommand(plugin));
		AddCommand(new HereCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		//Caller to Player
		if (args.length == 1)
		{
			if (_commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_COMMAND))
			{
				Plugin.playerToPlayer(caller, caller.getName(), args[0]);
			} else
			{
				UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			}
		}

		//Player to Player
		else if (args.length == 2)
		{
			if (_commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_OTHER_COMMAND))
			{
				Plugin.playerToPlayer(caller, args[0], args[1]);
			} else
			{
				UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			}
		}

		//Caller to Loc
		else if (args.length == 3)
		{
			if (_commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_LOCATION_COMMAND))
			{
				Plugin.playerToLoc(caller, caller.getName(), args[0], args[1], args[2]);
			} else
			{
				UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			}
		}
		
		//Player to world
		else if (args.length == 5)
		{
			if (_commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_LOCATION_COMMAND) && _commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_OTHER_COMMAND))
			{
				Plugin.playerToLoc(caller, args[0], args[1], args[2], args[3], args[4]);
			}
			else
			{
				UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			}
		}

		//Player to Loc
		else if (args.length == 4)
		{
			if (_commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_LOCATION_COMMAND) && _commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_OTHER_COMMAND))
			{
				Plugin.playerToLoc(caller, args[0], args[1], args[2], args[3]);
			}
			else
			{
				UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Commands List:"));
			UtilPlayer.message(caller, F.help("/tp <target>", "Teleport to Player", ChatColor.GOLD));
			UtilPlayer.message(caller, F.help("/tp b(ack) (amount) (player)", "Undo Teleports", ChatColor.GOLD));
			UtilPlayer.message(caller, F.help("/tp here <player>", "Teleport Player to Self", ChatColor.DARK_RED));
			UtilPlayer.message(caller, F.help("/tp <player> <target>", "Teleport Player to Player", ChatColor.DARK_RED));
			UtilPlayer.message(caller, F.help("/tp <X> <Y> <Z>", "Teleport to Location", ChatColor.DARK_RED));
			UtilPlayer.message(caller, F.help("/tp all", "Teleport All to Self", ChatColor.DARK_RED));
		}
	}
}