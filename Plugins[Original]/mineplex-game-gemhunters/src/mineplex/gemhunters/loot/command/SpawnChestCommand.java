package mineplex.gemhunters.loot.command;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.gemhunters.loot.LootModule;

public class SpawnChestCommand extends CommandBase<LootModule>
{	
	public SpawnChestCommand(LootModule plugin)
	{
		super(plugin, LootModule.Perm.SPAWN_CHEST_COMMAND, "spawnchest");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			caller.sendMessage(F.help("/" + _aliasUsed + " <colour>", "Spawns a chest at your location.", ChatColor.DARK_RED));
			return;
		}
		
		String colour = args[0].toUpperCase();
		
		try
		{
			DyeColor.valueOf(colour);
		}
		catch (IllegalArgumentException e)
		{
			caller.sendMessage(F.main(Plugin.getName(), "That is not a valid colour."));
			return;
		}
		
		caller.sendMessage(F.main(Plugin.getName(), "Spawned a " + colour + " chest at your location."));
		
		caller.getLocation().getBlock().setType(Material.CHEST);
		Plugin.addSpawnedChest(caller.getLocation(), colour);
	}
}