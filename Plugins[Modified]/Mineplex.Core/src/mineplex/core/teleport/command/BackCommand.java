package mineplex.core.teleport.command;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.core.teleport.Teleport;

public class BackCommand extends CommandBase<Teleport>
{
	public BackCommand(Teleport plugin)
	{
		super(plugin, Teleport.Perm.TELEPORT_COMMAND, "back", "b");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Back(caller, caller.getName(), "1");
		}
		else if (args.length == 1)
		{
			Back(caller, args[0], "1");
		}
		else
		{
			Back(caller, args[0], args[1]);
		}
	}
	
	private void Back(Player caller, String target, String amountString)
	{
		if (!target.equals(caller.getName()) && !_commandCenter.GetClientManager().Get(caller).hasPermission(Teleport.Perm.TELEPORT_OTHER_COMMAND))
		{
			UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			return;
		}
		
		int amount = 1;
		try
		{
			amount = Integer.parseInt(amountString);
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main("Teleport", "Invalid Amount [" + amountString + "]. Defaulting to [1]."));
		}

		Player player = UtilPlayer.searchOnline(caller, target, true);

		if (player == null)
		{
			return;
		}

		Location loc = null;
		int back = 0;
		for (int i = 0 ; i < amount ; i++)
		{
			if (Plugin.GetTPHistory(player) == null || Plugin.GetTPHistory(player).isEmpty())
			{
				break;
			}

			loc = Plugin.GetTPHistory(player).removeFirst();
			back++;
		}

		if (loc == null)
		{
			UtilPlayer.message(caller, F.main("Teleport", player.getName() + " has no teleport history."));
			return;
		}

		//Register
		String mA = F.main("Teleport", F.elem(caller.getName()) + " undid your last " + F.count(""+back) + " teleport(s).");
		String mB = F.main("Teleport", "You undid the last " + F.count(""+back) + " teleport(s) for " + F.elem(player.getName()) + ".");
		Plugin.Add(player, loc, mA, false, caller, mB,	"Undid last " + back + " teleports for " + player.getName() + " via " + caller.getName());
	}
}