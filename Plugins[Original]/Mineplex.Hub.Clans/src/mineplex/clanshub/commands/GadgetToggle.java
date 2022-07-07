package mineplex.clanshub.commands;

import org.bukkit.entity.Player;

import mineplex.clanshub.HubManager;
import mineplex.core.command.CommandBase;

/**
 * Command for toggling gadgets
 */
public class GadgetToggle extends CommandBase<HubManager>
{
	public GadgetToggle(HubManager plugin)
	{
		super(plugin, HubManager.Perm.GADGET_TOGGLE_COMMAND, "gadget");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.ToggleGadget(caller);
	}
}