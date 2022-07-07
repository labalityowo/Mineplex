package mineplex.hub.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.hub.HubManager;

public class GadgetToggle extends CommandBase<HubManager>
{
	public GadgetToggle(HubManager plugin)
	{
		super(plugin, HubManager.Perm.GADGET_TOGGLE_COMMAND, "gadget");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.toggleGadget();
	}
}