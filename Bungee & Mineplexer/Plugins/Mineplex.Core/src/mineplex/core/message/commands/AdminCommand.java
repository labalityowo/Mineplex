package mineplex.core.message.commands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.message.MessageManager;
import mineplex.core.recharge.Recharge;

public class AdminCommand extends CommandBase<MessageManager>
{
	public AdminCommand(MessageManager plugin)
	{
		super(plugin, MessageManager.Perm.ADMIN_COMMAND, "a","admin");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: " + F.elem("/a <message>")));
		}
		else
		{
			if (args.length == 0)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Message argument missing."));
				return;
			}
			
			if (Plugin.isMuted(caller))
			{
				return;
			}

			//Parse Message
			String message = F.combine(args, 0, null, false);

			//Inform
			UtilPlayer.message(caller, Plugin.GetClientManager().Get(caller).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true) + " " + caller.getName() + " " + C.cPurple + message);

			//Send
			for (Player to : UtilServer.getPlayers())
			{
				if (Plugin.GetClientManager().Get(to).hasPermission(MessageManager.Perm.SEE_ADMIN))
				{
					if (!to.equals(caller))
					{
						UtilPlayer.message(to, Plugin.GetClientManager().Get(caller).getRealOrDisguisedPrimaryGroup().getDisplay(true, false, false, true) + " " + caller.getName() + " " + C.cPurple + message);
					}

					//Sound
					to.playSound(to.getLocation(), Sound.NOTE_PLING, 0.5f, 2f);
				}
			}

			if (!Plugin.GetClientManager().Get(caller).hasPermission(MessageManager.Perm.SEE_ADMIN) && Recharge.Instance.use(caller, "AdminCommand.InformMsg", 60 * 1000, false, false))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "If there are any staff currently online in your server, you will receive a reply shortly."));
			}
		
			//Log XXX
			//Logger().logChat("Staff Chat", from, staff, message);
		}
	}
}