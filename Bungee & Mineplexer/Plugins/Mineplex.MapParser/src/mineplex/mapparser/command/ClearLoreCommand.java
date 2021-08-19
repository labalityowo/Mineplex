package mineplex.mapparser.command;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.mapparser.MapParser;

public class ClearLoreCommand extends BaseCommand
{
	public ClearLoreCommand(MapParser plugin)
	{
		super(plugin, "clearlore", "cl");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		ItemStack is = player.getItemInHand();

		if (is == null || is.getType() == Material.AIR)
		{
			message(player, "You must be holding an item in your hand.");
			return true;
		}

		ItemMeta im = is.getItemMeta();
		im.setLore(new ArrayList<>());
		is.setItemMeta(im);

		player.setItemInHand(is);
		player.updateInventory();
		message(player, "Cleared lore on item!");

		return true;
	}
}
