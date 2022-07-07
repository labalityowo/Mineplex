package mineplex.core.bonuses.gui.buttons;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.GuiItem;
import mineplex.core.gui.ItemRefresher;
import mineplex.core.gui.pages.LoadingWindow;
import mineplex.core.gui.pages.TimedMessageWindow;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.thank.ThankManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class ClaimTipsButton implements GuiItem, Listener
{
	private ItemStack _item;

	private Player _player;
	private Plugin _plugin;
	private ItemRefresher _gui;

	private BonusManager _bonusManager;
	private ThankManager _thankManager;

	public ClaimTipsButton(Plugin plugin, Player player, ItemRefresher gui, BonusManager bonusManager, ThankManager thankManager)
	{
		_bonusManager = bonusManager;
		_thankManager = thankManager;
		_player = player;
		_plugin = plugin;
		_gui = gui;
	}

	@Override
	public void setup()
	{
		Bukkit.getPluginManager().registerEvents(this, getPlugin());
		setItem();
	}

	@Override
	public void close()
	{
		HandlerList.unregisterAll(this);
	}

	@Override
	public void click(ClickType clickType)
	{
		if (!Recharge.Instance.use(_player, "Claim Tips Button", 1000, false, false))
		{
			return;
		}
		
		if (isAvailable()) {
			_item = ItemStackFactory.Instance.CreateStack(Material.LAPIS_BLOCK, (byte)0, 1, ChatColor.BLUE + "Processing...");
			refreshItem();
			new LoadingWindow(getPlugin(), getPlayer(), 6*9);
			_thankManager.claimThanks(getPlayer(), claimThankResult -> {
				if (claimThankResult != null && claimThankResult.getClaimed() > 0)
				{
					// Woo, success!
					setItem();

					if (getPlayer().getOpenInventory() != null)
					{
						new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData(), 1, ChatColor.GREEN + "Amplifier Thanks Collected"), "Thanks Collected", 6*9, 20*3, getGui()).openInventory();
					}

					UtilPlayer.message(getPlayer(), F.main("Carl", "You collected " + F.currency(GlobalCurrency.TREASURE_SHARD, claimThankResult.getClaimed()) + " from " + F.elem(claimThankResult.getUniqueThanks()) + " players!"));
					// Pending explosions are strange.. Not sure why we are using strings. Either way, lets display a rank reward effect
					_bonusManager.addPendingExplosion(getPlayer(), "RANK");
					getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1.6f);
				}
				else if (claimThankResult != null && claimThankResult.getClaimed() == 0)
				{
					// No tips to claim
					if (getPlayer().getOpenInventory() != null)
					{
						new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.RED.getData(), 1, ChatColor.RED + "No Thanks to Claim!"), "You have no thank to claim!", 6*9, 20*3, getGui()).openInventory();
					}

					UtilPlayer.message(getPlayer(), F.main("Carl", "You have no rewards to claim!"));
					getPlayer().playSound(getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1, 10);
				}
				else
				{
					// Failed to claim
					if (getPlayer().getOpenInventory() != null)
					{
						new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.RED.getData(), 1, ChatColor.RED + "Error collecting rewards. Try again later."), "Error", 6*9, 20*3, getGui()).openInventory();
					}

					UtilPlayer.message(getPlayer(), F.main("Carl", "Error collecting rewards. Try again later."));
					getPlayer().playSound(getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1, 10);
				}

				getPlayer().closeInventory();
			});
		}
		else
		{
			getPlayer().playSound(getPlayer().getLocation(), Sound.ITEM_BREAK, 1, 10);
		}
	}

	private void setItem()
	{
		ArrayList<String> lore = new ArrayList<String>();
		Material material;
		String itemName;

		if (isAvailable())
		{
			material = Material.EMERALD;
			itemName = C.cGreen + C.Bold + "Thank Rewards";
			lore.add(" ");
			lore.add(C.cYellow + "Your Rewards");
			lore.add("  " + C.cWhite + getTips() + " Treasure Shards");
			lore.add(" ");
			lore.add(ChatColor.RESET + "Click to Claim!");
		}
		else
		{
			material = Material.REDSTONE_BLOCK;
			itemName = C.cRed + C.Bold + "Thank Rewards";

			lore.add(" ");
			lore.add(C.cGray + "Earn Thank Rewards from players using /thank");
			lore.add(C.cGray + "on you, or by enabling Game Amplifiers.");
			lore.add(" ");
			lore.add(C.cWhite + "Get Amplifiers at " + C.cGreen + "mineplex.com/shop");
		}

		_item =  new ShopItem(material, itemName, lore.toArray(new String[0]), 1, false, false);
	}

	@Override
	public ItemStack getObject()
	{
		return _item;
	}

	public void refreshItem()
	{
		getGui().refreshItem(this);
	}

	private int getTips()
	{
		return _thankManager.Get(_player).getThankToClaim();
	}

	private boolean isAvailable()
	{
		return getTips() > 0;
	}

	public Plugin getPlugin()
	{
		return _plugin;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public ItemRefresher getGui()
	{
		return _gui;
	}
}