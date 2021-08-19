package nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.ArcadeManager.Perm;
import nautilus.game.arcade.game.Game;

public class MapCommand extends CommandBase<ArcadeManager>
{

	public MapCommand(ArcadeManager plugin)
	{
		super(plugin, Perm.MAP_COMMAND, "whatmap", "mapinfo");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Game game = Plugin.GetGame();

		if (game == null || game.WorldData == null || game.WorldData.MapName == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), "There is currently no map running."));
			return;
		}

		caller.sendMessage(F.main(Plugin.getName(), game.WorldData.getFormattedName()));
	}
}
