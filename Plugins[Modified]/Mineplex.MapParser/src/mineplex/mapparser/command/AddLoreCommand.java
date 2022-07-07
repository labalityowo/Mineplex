package mineplex.mapparser.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;

public class AddLoreCommand extends BaseCommand
{
	public AddLoreCommand(MapParser plugin)
	{
		super(plugin, "addlore", "al");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args == null || args.length < 1)
		{
			message(player, "Invalid Usage: " + F.elem("/" + alias + " <text>"));
			return true;
		}

		ItemStack is = player.getItemInHand();

		if (is == null || is.getType() == Material.AIR)
		{
			message(player, "You must be holding an item in your hand.");
			return true;
		}

		ItemMeta im = is.getItemMeta();

		StringBuilder line = new StringBuilder();
		for (String arg : args)
		{
			line.append(arg).append(" ");
		}
		line = new StringBuilder(line.toString().replaceAll("&", "§").trim());

		List<String> lore = (im.getLore() != null ? new ArrayList<>(im.getLore()) : new ArrayList<>());
		lore.add(line.toString());
		im.setLore(lore);
		is.setItemMeta(im);

		player.setItemInHand(is);
		player.updateInventory();
		message(player, "Added lore: " + line);

		return true;
	}
}
