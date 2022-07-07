package mineplex.gemhunters.loot.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.gemhunters.loot.LootModule;

/**
 * An ADMIN command that allows users to retrieve the latest data from the
 * google sheet and update all locally cached loot tables.
 */
public class UpdateLootCommand extends CommandBase<LootModule>
{
	public UpdateLootCommand(LootModule plugin)
	{
		super(plugin, LootModule.Perm.UPDATE_LOOT_COMMAND, "updateloot");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length > 1)
		{
			// TODO send redis message
		}

		caller.sendMessage(F.main(Plugin.getName(), "This command is currently disabled due to development issues."));
	}
}