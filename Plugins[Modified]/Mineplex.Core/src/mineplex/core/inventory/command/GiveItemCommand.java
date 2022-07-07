package mineplex.core.inventory.command;

import java.util.UUID;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.inventory.data.Item;

public class GiveItemCommand extends CommandBase<InventoryManager>
{
	public GiveItemCommand(InventoryManager plugin)
	{
		super(plugin, InventoryManager.Perm.GIVE_ITEM_COMMAND, "giveitem");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null || args.length < 3)
		{
			displayUsage(caller);
			return;
		}

		final String playerName = args[0];
		final int amount = Integer.parseInt(args[1]);
		String itemNameTemp = "";
		for (int i = 2; i < args.length; i++)
		{
			itemNameTemp += args[i] + " ";
		}

		itemNameTemp = itemNameTemp.trim();
		
		final String itemName = itemNameTemp;

		final Item item = Plugin.getItem(itemName);
		Player player = UtilPlayer.searchExact(playerName);

		if (item == null)
		{
			UtilPlayer.message(caller, F.main("Item", "Item with the name " + F.item(itemName) + " not found!"));
		}
		else if (playerName.equalsIgnoreCase("@a"))
		{
			for (Player pl : UtilServer.getPlayers())
			{
				Plugin.addItemToInventory(pl, item.Name, amount);
			}
			
			UtilPlayer.message(caller, F.main("Item", "You gave " + F.elem(amount + " " + itemName) + " to everyone"));
		}
		else if (player != null)
		{
			Plugin.addItemToInventory(player, item.Name, amount);
			UtilPlayer.message(caller, F.main("Item", "You gave " + F.elem(amount + " " + itemName) + " to player " + F.name(playerName)));
			UtilPlayer.message(player, F.main("Item", F.name(caller.getName()) + " gave you " + F.elem(amount + " " + itemName)));
		}
		else
		{
			Plugin.runAsync(() ->
			{
				UUID uuid = Plugin.getClientManager().loadUUIDFromDB(playerName);

				if (uuid != null)
				{
					Plugin.addItemToInventoryForOffline(success ->
					{
						if (success)
						{
							UtilPlayer.message(caller, F.main("Item", "You gave " + F.elem(amount + " " + itemName) + " to offline player " + F.name(playerName)));
						}
						else
						{
							UtilPlayer.message(caller, F.main("Item", "An error occured while trying to give item to " + F.name(playerName) + "!"));
						}
					}, uuid, item.Name, amount);
				}
				else
				{
					UtilPlayer.message(caller, F.main("Item", "Player " + F.name(playerName) + " does not exist!"));
				}
			});
		}
	}

	private void displayUsage(Player caller)
	{
		UtilPlayer.message(caller, F.main("Item", "Usage: " + F.elem("/giveitem <playername> <amount> <item name>")));
	}
}