package mineplex.core.bonuses.gui.buttons;

import java.util.ArrayList;

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

import mineplex.core.bonuses.BonusAmount;
import mineplex.core.bonuses.BonusClientData;
import mineplex.core.bonuses.BonusManager;
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

public class DailyBonusButton implements GuiItem, Listener
{
	private ItemStack _item;
	
	private Player _player;
	private Plugin _plugin;
	private ItemRefresher _gui;
	
	private BonusManager _bonusManager;
	
	public DailyBonusButton(Plugin plugin, Player player, ItemRefresher gui, BonusManager bonusManager)
	{
		this._bonusManager = bonusManager;
		this._player = player;
		this._plugin = plugin;
		this._gui = gui;
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
		if (!Recharge.Instance.use(_player, "Carl Daily Bonus", 1000, false, false))
		{
			return;
		}
		
		if (isAvailable() && canUse())
		{
			_item = ItemStackFactory.Instance.CreateStack(Material.LAPIS_BLOCK, (byte)0, 1, ChatColor.BLUE + "Processing...");
			refreshItem();
			new LoadingWindow(getPlugin(), getPlayer(), 6*9);
			_bonusManager.attemptDailyBonus(getPlayer(), _bonusManager.ClansBonus ? _bonusManager.getClansDailyBonusAmount(_player) : _bonusManager.getDailyBonusAmount(_player), _bonusManager.ClansBonus, new Callback<Boolean>()
			{
				@Override
				public void run(Boolean t)
				{
					if (t)
					{
						setItem();

						if (getPlayer().getOpenInventory() != null)
						{
							new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData(), 1, ChatColor.GREEN + "Bonus collected!"), "Bonus collected!", 6*9, 20*3, getGui()).openInventory();
						}
						else
						{
							UtilPlayer.message(getPlayer(), F.main("Bonus", "Bonus collected!"));
						}
						_bonusManager.addPendingExplosion(getPlayer(), "DAILY");
						getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1.6f);
					}
					else
					{
						if (getPlayer().getOpenInventory() != null)
						{
							new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.STAINED_GLASS_PANE, DyeColor.RED.getData(), 1, ChatColor.RED + "Failed to collect bonus!"), "Failed to collect bonus!", 6*9, 20*3, getGui()).openInventory();
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
		}
		else
		{
			getPlayer().playSound(getPlayer().getLocation(), Sound.ITEM_BREAK, 1, 10);
		}
		return;
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!event.getType().equals(UpdateType.SEC))
			return;
//		refreshItem(); // Todo Unnecessary?
	}

	private void setItem()
	{
		ArrayList<String> lore = new ArrayList<String>();
		Material material;
		String itemName;
		byte data = 0;

		if (isAvailable() && canUse())
		{
			material = Material.CHEST;
			itemName = C.cGreen + C.Bold + "Daily Reward";

			lore.add(" ");
			lore.add(ChatColor.RESET + "Click to Claim!");
		}
		else if (!canUse())
		{
			material = Material.REDSTONE_BLOCK;
			itemName = C.cRed + C.Bold + "Daily Reward";

			lore.add(" ");
			lore.add(ChatColor.RESET + "You must set your home server by joining a clan to claim this!");
		}
		else
		{
			material = Material.REDSTONE_BLOCK;
			itemName = C.cRed + C.Bold + "Daily Reward";

			lore.add(" ");
			lore.add(ChatColor.RESET + "Next reward in " + UtilTime.convertString(timeLeft(), 0, TimeUnit.FIT) + "!");
		}

		lore.add(" ");
		
		if (!_bonusManager.ClansBonus)
		{
			BonusClientData client = _bonusManager.Get(_player);
			
			BonusAmount bonusAmount = _bonusManager.getDailyBonusAmount(_player);
			bonusAmount.addLore(lore);
			lore.add(" ");

			lore.add(C.cYellow + "Current Streak: " + C.cWhite + client.getDailyStreak());
			lore.add(C.cYellow + "Streak Bonus: " + C.cWhite + _bonusManager.getDailyMultiplier(_player) + "%");
			lore.add(" ");
			lore.add(C.cYellow + "Highest Streak: " + C.cWhite + client.getMaxDailyStreak());

			if (client.getDailyTime() != null)
			{
				long lastBonus = BonusManager.getLocalTime(client.getDailyTime().getTime());
				long timeLeft = _bonusManager.getStreakTimeRemaining(lastBonus, BonusManager.DAILY_STREAK_RESET_TIME);

				if (timeLeft > 0)
				{
					lore.add(C.cYellow + "Streak Reset: " + C.cWhite + UtilTime.convertString(timeLeft, 1, TimeUnit.FIT));
				}
			}
			
//			StreakRecord streakRecord = _bonusManager.getDailyStreak();
//			if (streakRecord != null)
//			{
//				lore.add(" ");
//				lore.add(C.cYellow + "Record: " + C.cWhite + streakRecord.getPlayerName());
//				lore.add(C.cYellow + "Streak: " + C.cWhite + streakRecord.getStreak());
//			}
		}
		else
		{
			lore.add(C.cYellow + "Home Server: " + C.cWhite + _bonusManager.getClansHomeServer(getPlayer()).getLeft());
			lore.add(" ");
			BonusAmount amount = _bonusManager.getClansDailyBonusAmount(_player);
			amount.addLore(lore);
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
	
	public long timeLeft()
	{
		long timeLeft = _bonusManager.nextDailyBonus(getPlayer()) - System.currentTimeMillis();
		return timeLeft;
	}
	
	public boolean isAvailable()
	{
		return (timeLeft() <= 0);
	}
	
	public boolean canUse()
	{
		if (!_bonusManager.ClansBonus)
		{
			return true;
		}
		
		return _bonusManager.getClansHomeServer(getPlayer()).getRight() != -1;
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