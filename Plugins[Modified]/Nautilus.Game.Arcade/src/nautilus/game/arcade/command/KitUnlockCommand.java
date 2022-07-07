package nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import nautilus.game.arcade.ArcadeManager;

public class KitUnlockCommand extends CommandBase<ArcadeManager>
{
	public KitUnlockCommand(ArcadeManager plugin)
	{
		super(plugin, ArcadeManager.Perm.KIT_UNLOCK_COMMAND, "youtube", "twitch", "kits");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.toggleUnlockKits(caller);
	}
}