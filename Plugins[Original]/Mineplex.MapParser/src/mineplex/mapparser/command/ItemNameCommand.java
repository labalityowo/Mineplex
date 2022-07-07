package mineplex.mapparser.command;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemNameCommand extends BaseCommand
{
    public ItemNameCommand(MapParser plugin)
    {
        super(plugin, "itemname", "in");
    }

    @Override
    public boolean execute(Player player, String alias, String[] args)
    {
        if (args == null || args.length < 1)
        {
            message(player, "Invalid Usage: " + F.elem("/" + alias + " <name>"));
            return true;
        }

        ItemStack is = player.getItemInHand();

        if (is == null || is.getType() == Material.AIR)
        {
            message(player, "You must be holding an item in your hand.");
            return true;
        }

        ItemMeta im = is.getItemMeta();

        StringBuilder name = new StringBuilder();
        for (String arg : args)
        {
            name.append(arg).append(" ");
        }
        name = new StringBuilder(name.toString().replaceAll("&", "§").trim());

        im.setDisplayName(name.toString());
        is.setItemMeta(im);

        player.setItemInHand(is);
        player.updateInventory();
        message(player, "Set name: " + name);

        return true;
    }
}
