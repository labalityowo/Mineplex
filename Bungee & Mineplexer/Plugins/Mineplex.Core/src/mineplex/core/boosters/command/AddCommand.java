package mineplex.core.boosters.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.boosters.BoosterApiResponse;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

/**
 * @author Shaun Bennett
 */
public class AddCommand extends CommandBase<BoosterManager>
{
	public AddCommand(BoosterManager plugin)
	{
		super(plugin, BoosterManager.Perm.ADD_BOOSTER_COMMAND, "add");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args != null && args.length > 0)
		{
			String serverGroup = args[0];
			Plugin.activateBooster(serverGroup, caller, new Callback<BoosterApiResponse>()
			{
				@Override
				public void run(BoosterApiResponse response)
				{
					if (response.isSuccess())
					{
						UtilPlayer.message(caller, F.main("Amplifier", "Successfully added amplifier to " + F.elem(serverGroup)));
					}
					else
					{
						UtilPlayer.message(caller, F.main("Amplifier", "Failed to add amplifier. Error: " + F.elem(response.getError())));
					}
				}
			});
		}
		else
		{
			help(caller);
		}
	}

	private void help(Player caller)
	{
		UtilPlayer.message(caller, F.help("amplifier add <servergroup>", "Add an amplifier to that server group", ChatColor.DARK_RED));
	}
}