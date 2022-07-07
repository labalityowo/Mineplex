package mineplex.mapparser.command;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 *
 */
public class PlayerHeadCommand extends BaseCommand
{

	public PlayerHeadCommand(MapParser plugin, String... aliases)
	{
		super(plugin, "playerhead");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if(args.length == 1) {
			String name = args[0];
			ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
			SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
			meta.setOwner(name);
			itemStack.setItemMeta(meta);
			player.getInventory().addItem(itemStack);
			player.sendMessage(C.cGray + "Given " + F.elem(name) + "'s head");
			return true;
		}
		return false;
	}
}
