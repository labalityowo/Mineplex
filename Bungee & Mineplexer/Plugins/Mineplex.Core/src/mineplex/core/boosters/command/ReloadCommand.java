package mineplex.core.boosters.command;

import org.bukkit.entity.Player;

import mineplex.core.boosters.BoosterManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

/**
 * @author Shaun Bennett
 */
public class ReloadCommand extends CommandBase<BoosterManager>
{
	public ReloadCommand(BoosterManager plugin)
	{
		super(plugin, BoosterManager.Perm.RELOAD_BOOSTERS_COMMAND, "reload");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.getBoostersAsync(data ->
		{
			if (data != null)
			{
				UtilPlayer.message(caller, F.main("Amplifier", "Amplifiers reloaded!"));
			}
		});
	}
}