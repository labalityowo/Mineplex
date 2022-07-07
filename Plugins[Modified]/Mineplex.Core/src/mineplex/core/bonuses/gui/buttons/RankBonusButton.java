package mineplex.core.bonuses.gui.buttons;

import java.util.ArrayList;

import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.gui.GuiItem;
import mineplex.core.gui.ItemRefresher;
import mineplex.core.gui.pages.LoadingWindow;
import mineplex.core.gui.pages.TimedMessageWindow;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.bonuses.BonusAmount;
import mineplex.core.bonuses.BonusManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class RankBonusButton implements GuiItem, Listener {
	
	private boolean hasRank;
	
	private ItemStack _item;
	
	private Player _player;
	private Plugin _plugin;
	private ItemRefresher _gui;
	
	private BonusManager _bonusManager;
	
	public RankBonusButton(Plugin plugin, Player player, ItemRefresher gui, BonusManager bonusManager)
	{
		this._bonusManager = bonusManager;
		this._player = player;
		this._plugin = plugin;
		this._gui = gui;
	}
	
	@Override
	public void setup()
	{
		if (_bonusManager.getRankBonusAmount(getPlayer()).isGreaterThanZero())
		{
			this.hasRank = true;
			Bukkit.getPluginManager().registerEvents(this, getPlugin());
		}
		else
		{
			this.hasRank = false;
		}

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
		if (!Recharge.Instance.use(_player, "Claim Rank Bonus", 1000, false, false))
		{
			return;
		}
		
		if (isAvailable() && _bonusManager.isPastAugust()) {
			_item = ItemStackFactory.Instance.CreateStack(Material.LAPIS_BLOCK, (byte)0, 1, ChatColor.BLUE + "Processing...");
			refreshItem();
			new LoadingWindow(getPlugin(), getPlayer(), 6*9);
			_bonusManager.attemptRankBonus(getPlayer(), new Callback<Boolean>()
			{
				@Override
				public void run(Boolean t)
				{
					setItem();

					if (t)
					{
						if (getPlayer().getOpenInventory() != null)
						{
							new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData(), 1, ChatColor.GREEN + "Bonus collected!"), "Bonus collected!", 6 * 9, 20 * 3, getGui()).openInventory();
						}
						else
						{
							UtilPlayer.message(getPlayer(), F.main("Bonus", "Bonus collected!"));
						}
						_bonusManager.addPendingExplosion(getPlayer(), "RANK");
						getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1.6f);
					}
					else
					{
						if (getPlayer().getOpenInventory() != null)
						{
							new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.RED.getData(), 1, ChatColor.RED + "Failed to collect bonus!"), "Failed to collect bonus!", 6 * 9, 20 * 3, getGui()).openInventory();
						}
						else
						{
							UtilPlayer.message(getPlayer(), F.main("Bonus", "Failed to collect bonus!"));
						}
						getPlayer().playSound(getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1, 10);
					}
					getPlayer().closeInventory();
				}
			});
		} else
			getPlayer().playSound(getPlayer().getLocation(), Sound.ITEM_BREAK, 1, 10);
		return;

	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!event.getType().equals(UpdateType.SEC))
			return;
//		refreshItem(); // Todo Unnecessary?
	}
	
	@Override
	public ItemStack getObject()
	{
		return _item;
	}

	private void setItem()
	{
		ArrayList<String> lore = new ArrayList<String>();
		Material material;
		String itemName;
		byte data = 0;

		if (_bonusManager.isPastAugust())
		{
			if (!hasRank)
			{
				material = Material.REDSTONE_BLOCK;
				itemName = C.cRed + ChatColor.BOLD + "Rank Monthly Bonus";
				lore.add(" ");
				lore.add(ChatColor.WHITE + "Players with a Rank get a Monthly Bonus!");
				lore.add(ChatColor.WHITE + "");
				lore.add(ChatColor.AQUA + "Ultra receives 1 Mythical Chest Monthly");
				lore.add(ChatColor.LIGHT_PURPLE + "Hero receives 2 Mythical Chests Monthly");
				lore.add(ChatColor.GREEN + "Legend receives 3 Mythical Chests Monthly");
				lore.add(ChatColor.RED + "Titan receives 5 Mythical Chests Monthly");
				lore.add(ChatColor.WHITE + "");
				lore.add(ChatColor.WHITE + "Purchase a Rank at;");
				lore.add(ChatColor.WHITE + "www.mineplex.com/shop");
			}
			else
			{
				if (isAvailable())
				{
					material = Material.ENDER_CHEST;
					itemName = C.cGreen + C.Bold + "Rank Monthly Bonus";

					lore.add(" ");
					lore.add(ChatColor.RESET + "Click to Claim!");
				}
				else
				{
					material = Material.REDSTONE_BLOCK;
					itemName = C.cRed + C.Bold + "Rank Monthly Bonus";

					lore.add(" ");
					lore.add(ChatColor.RESET + "Next reward in " + UtilTime.convertString(timeLeft(), 0, TimeUnit.FIT) + "!");
				}

				lore.add(" ");
				lore.add(C.cYellow + "Rank: " + C.cWhite + _bonusManager.getClientManager().Get(_player).getPrimaryGroup().getDisplay(false, false, false, true));
				BonusAmount bonusAmount = _bonusManager.getRankBonusAmount(_player);
				bonusAmount.addLore(lore);
			}
		}
		else
		{
			itemName = C.cRed + ChatColor.BOLD + "Rank Monthly Bonus";
			material = Material.REDSTONE_BLOCK;
			lore.add(" ");
			lore.add(ChatColor.RESET + "You can claim your Monthly Bonus");
			lore.add(ChatColor.RESET + "here, starting from September!");
		}


		_item = new ShopItem(material, itemName, lore.toArray(new String[0]), 1, false, false);
	}

	public void refreshItem()
	{
		_gui.refreshItem(this);
	}
	
	public long timeLeft()
	{
		return _bonusManager.nextRankBonus(getPlayer()) - System.currentTimeMillis();
	}
	
	public boolean isAvailable()
	{
		if (!hasRank)
		{
			UtilPlayer.message(getPlayer(), "----------------------------------------");
			UtilPlayer.message(getPlayer(), "");
			UtilPlayer.message(getPlayer(), "Purchase a Rank at the Mineplex Shop:");
			UtilPlayer.message(getPlayer(), C.cGreen + "www.mineplex.com/shop");
			UtilPlayer.message(getPlayer(), "");
			UtilPlayer.message(getPlayer(), "----------------------------------------");
			
			getPlayer().closeInventory();
			return false;
		}
		return (timeLeft() <= 0);
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