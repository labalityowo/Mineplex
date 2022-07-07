package mineplex.core.ignore.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.ignore.IgnoreManager;

public class Unignore extends CommandBase<IgnoreManager>
{
	public Unignore(IgnoreManager plugin)
	{
		super(plugin, IgnoreManager.Perm.IGNORE_COMMAND, "unignore");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), "You need to include a player's name."));
		}
		else
		{
			_commandCenter.GetClientManager().checkPlayerName(caller, args[0], new Callback<String>()
			{
				public void run(String result)
				{
					if (result != null)
					{
						Plugin.removeIgnore(caller, result);
					}
				}
			});			
		}
	}
}