package mineplex.core.disguise.playerdisguise;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class DisguiseCommand extends CommandBase<PlayerDisguiseManager> implements Listener
{
	DisguiseCommand(PlayerDisguiseManager plugin)
	{
		super(plugin, PlayerDisguiseManager.Perm.USE_DISGUISE, "disguise");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		String realName = Plugin.getRealName(caller);
		String currentName = caller.getName();
		UUID currentUUID = caller.getUniqueId();
		if (args == null || args.length == 0)
		{
			Plugin.runAsync(() ->
			{
				new PlayerDisguiseNotification(realName, currentUUID, currentName).publish();
			});
			Plugin.undisguise(caller);
			return;
		}
		if (args.length > 2)
		{
			UtilPlayer.message(caller, F.help("/disguise <username> [username of skin]", "Disguise yourself as 'username' with optional skin belonging to 'username of skin'", ChatColor.DARK_RED));
			return;
		}

		String skin = args.length > 1 ? args[1] : args[0];
		Plugin.tryDisguise(caller, args[0], skin, () -> // onComplete
				Plugin.runAsync(() -> // task
						new PlayerDisguiseNotification(realName, currentUUID, args[0], skin).publish()));
	}
}