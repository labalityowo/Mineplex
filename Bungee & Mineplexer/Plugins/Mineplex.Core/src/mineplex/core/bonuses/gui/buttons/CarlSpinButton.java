package mineplex.core.bonuses.gui.buttons;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import mineplex.core.common.util.C;
import mineplex.core.gui.GuiItem;
import mineplex.core.treasure.reward.TreasureRewardManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.bonuses.BonusClientData;
import mineplex.core.bonuses.BonusManager;

public class CarlSpinButton implements GuiItem
{
	private Plugin _plugin;
	private Player _player;
	private BonusManager _bonusManager;
	private TreasureRewardManager _rewardManager;

	public CarlSpinButton(Plugin plugin, Player player, BonusManager bonusManager, TreasureRewardManager rewardManager)
	{
		_plugin = plugin;
		_player = player;
		_bonusManager = bonusManager;
		_rewardManager = rewardManager;
	}

	@Override
	public void setup()
	{
	}

	@Override
	public void close()
	{

	}

	@Override
	public void click(ClickType clickType)
	{
		BonusClientData client = _bonusManager.Get(_player);
		int tickets = client.getTickets();

		if (tickets > 0)
		{
			_bonusManager.attemptCarlSpin(_player);
		}
			
//		new SpinGui(_plugin, _player, _rewardManager).openInventory();
	}

	@Override
	public ItemStack getObject()
	{
		BonusClientData client = _bonusManager.Get(_player);
		int tickets = client.getTickets();

		String name = (tickets > 0 ? C.cGreen : C.cRed) + C.Bold + "Carl's Spinner";
		ArrayList<String> lore = new ArrayList<String>();
		Material material = Material.SKULL_ITEM;
		byte data = (byte) 4;

		lore.add(" ");
		if (tickets > 0)
		{
			lore.add(ChatColor.RESET + "Click to Spin");
		}
		else
		{
			lore.add(ChatColor.RESET + "You need a Carl Spin Ticket to Spin");
		}

		lore.add(" ");
		lore.add(ChatColor.YELLOW + "Your Tickets: " + C.cWhite + tickets);

		return new ShopItem(material, data, name, lore.toArray(new String[0]), 1, false, false);
	}
}