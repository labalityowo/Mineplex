package mineplex.core.boosters.command;

import org.bukkit.entity.Player;

import mineplex.core.boosters.BoosterManager;
import mineplex.core.command.CommandBase;

/**
 * @author Shaun Bennett
 */
public class GuiCommand extends CommandBase<BoosterManager>
{
	public GuiCommand(BoosterManager plugin)
	{
		super(plugin, BoosterManager.Perm.BOOSTER_GUI_COMMAND, "gui");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.openShop(caller);
	}
}