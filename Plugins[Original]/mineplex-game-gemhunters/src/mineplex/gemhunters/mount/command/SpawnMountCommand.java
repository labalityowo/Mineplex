package mineplex.gemhunters.mount.command;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.command.CommandBase;
import mineplex.gemhunters.mount.MountData;
import mineplex.gemhunters.mount.MountModule;
import mineplex.gemhunters.mount.MountModule.Perm;

public class SpawnMountCommand extends CommandBase<MountModule>
{

	public SpawnMountCommand(MountModule plugin)
	{
		super(plugin, Perm.SPAWN_MOUNT_COMMAND, "spawnmount");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		MountData data = Plugin.Get(caller);

		if (data.getEntity() != null)
		{
			data.getEntity().remove();
		}

		Plugin.spawnHorse(caller, data, new ItemStack(Material.DIAMOND_BARDING), 0);
	}
}
