package mineplex.core.give;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.structs.ItemContainer;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.give.commands.GiveCommand;
import mineplex.core.itemstack.ItemStackFactory;

public class Give extends MiniPlugin
{
	public enum Perm implements Permission
	{
		GIVE_COMMAND,
	}

	public static Give Instance;

	protected Give(JavaPlugin plugin)
	{
		super("Give Factory", plugin);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.GIVE_COMMAND, true, true);
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QA.setPermission(Perm.GIVE_COMMAND, true, true);
		}
	}

	public static void Initialize(JavaPlugin plugin)
	{
		Instance = new Give(plugin);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new GiveCommand(this));
	}

	public void parseInput(Player player, String[] args) 
	{
		if (args.length == 0)
			help(player);

		else if (args.length == 1)
			give(player, player.getName(), args[0], "1", "");

		else if (args.length == 2)
			give(player, args[0], args[1], "1", "");

		else if (args.length == 3)
			give(player, args[0], args[1], args[2], "");
		
		else 
			give(player, args[0], args[1], args[2], args[3]);
	}

	public void help(Player player)
	{
		UtilPlayer.message(player, F.main("Give", "Commands List;"));
	}

	public void give(Player player, String target, String itemNames, String amount, String enchants)
	{
		//Item
		LinkedList<ItemContainer> itemList = new LinkedList<ItemContainer>();
		itemList = UtilItem.matchItem(player, itemNames, true);
		if (itemList.isEmpty())			
			return;

		//Player
		LinkedList<Player> giveList = new LinkedList<Player>();

		if (target.equalsIgnoreCase("all"))
		{
			for (Player cur : UtilServer.getPlayers())
				giveList.add(cur);
		}
		else
		{
			giveList = UtilPlayer.matchOnline(player, target, true);
			if (giveList.isEmpty())			
				return;
		}


		//Amount
		int count = 1;
		try
		{
			count = Integer.parseInt(amount);

			if (count < 1)
			{
				UtilPlayer.message(player, F.main("Give", "Invalid Amount [" + amount + "]. Defaulting to [1]."));
				count = 1;
			}
		}
		catch (Exception e)
		{
			UtilPlayer.message(player, F.main("Give", "Invalid Amount [" + amount + "]. Defaulting to [1]."));
		}
		
		//Enchants
		Map<Enchantment, Integer> enchs = new HashMap<>();
		if (enchants.length() > 0)
		{
			for (String cur : enchants.split(","))
			{
				try
				{
					String[] tokens = cur.split(":");
					enchs.put(Enchantment.getByName(tokens[0]), Integer.parseInt(tokens[1]));
				}
				catch (Exception e)
				{
					UtilPlayer.message(player, F.main("Give", "Invalid Enchantment [" + cur + "]."));
				}
			}
		}
		
		//Create
		String givenList = "";
		for (Player cur : giveList)
			givenList += cur.getName() + " ";
		if (givenList.length() > 0)
			givenList = givenList.substring(0, givenList.length()-1);

		for (ItemContainer curItem : itemList)
		{
			for (Player cur : giveList)
			{
				ItemStack stack;
				if (curItem.Name == null)
					stack = ItemStackFactory.Instance.CreateStack(curItem.Type, curItem.Data, count);
				else
					stack = ItemStackFactory.Instance.CreateStack(curItem.Type, curItem.Data, count, curItem.Name);
				
				//Enchants
				stack.addUnsafeEnchantments(enchs);
				
				//Give
				if (UtilInv.insert(cur, stack))
				{
					//Inform
					if (!cur.equals(player))
						UtilPlayer.message(cur, F.main("Give", "You received " + F.item(count + " " + ItemStackFactory.Instance.GetName(curItem.Type, curItem.Data, false)) + " from " + F.elem(player.getName()) + "."));
				}
			}

			if (target.equalsIgnoreCase("all"))
				UtilPlayer.message(player, F.main("Give", "You gave " + F.item(count + " " + ItemStackFactory.Instance.GetName(curItem.Type, curItem.Data, false)) + " to " + F.elem("ALL")) + ".");

			else if (giveList.size() > 1)
				UtilPlayer.message(player, F.main("Give", "You gave " + F.item(count + " " + ItemStackFactory.Instance.GetName(curItem.Type, curItem.Data, false)) + " to " + F.elem(givenList) + "."));

			else
				UtilPlayer.message(player, F.main("Give", "You gave " + F.item(count + " " + ItemStackFactory.Instance.GetName(curItem.Type, curItem.Data, false)) + " to " + F.elem(giveList.getFirst().getName()) + "."));
		}
	}
}