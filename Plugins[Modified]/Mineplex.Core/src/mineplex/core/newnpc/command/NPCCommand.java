package mineplex.core.newnpc.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.NewNPCManager.Perm;

public class NPCCommand extends MultiCommandBase<NewNPCManager>
{

	public NPCCommand(NewNPCManager plugin)
	{
		super(plugin, Perm.NPC_COMMAND, "newnpc");

		AddCommand(new NPCBuildCommand(plugin));
		AddCommand(new NPCDeleteCommand(plugin));
		AddCommand(new NPCClearCommand(plugin));
		AddCommand(new NPCEditCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Command List:"));
		caller.sendMessage(F.help("/" + _aliasUsed + " builder", "Creates a new NPC using the builder.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " delete", "Deletes an NPC.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " clear", "Removes all NPCs permanently.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " edit", "Loads the edit menu for an npc.", ChatColor.DARK_RED));
	}
}
