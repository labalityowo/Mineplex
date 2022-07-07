package mineplex.core.bonuses.gui.buttons;

import java.util.ArrayList;

import mineplex.core.bonuses.BonusAmount;
import mineplex.core.bonuses.BonusClientData;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.gui.GuiItem;
import mineplex.core.gui.ItemRefresher;
import mineplex.core.shop.item.ShopItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class VoteButton implements GuiItem, Listener
{
	private ItemStack _item;
	
	private String _url;
	
	private Player _player;
	private Plugin _plugin;
	private ItemRefresher _gui;
	
	private BonusManager _bonusManager;
	
	public VoteButton(Plugin plugin, Player player, ItemRefresher gui, BonusManager bonusManager)
	{
		_bonusManager = bonusManager;
		_player = player;
		_plugin = plugin;
		_gui = gui;
	}

	@Override
	public void setup()
	{
		//TODO get url from db
		_url = _bonusManager.getVoteLink();

		setItem();
		Bukkit.getPluginManager().registerEvents(this, getPlugin());
	}

	@Override
	public void close()
	{
		HandlerList.unregisterAll(this);
	}

	@Override
	public void click(ClickType clickType)
	{
		if (isAvailable())
		{
			getPlayer().closeInventory();
			
			getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1.6f);
			
			UtilPlayer.message(getPlayer(), C.cGold + C.Bold + C.Strike + "=============================================");
			UtilPlayer.message(getPlayer(), "");

			new JsonMessage("      " + C.Bold + "Click to Open in Web Browser").click(ClickEvent.OPEN_URL, _url).sendToPlayer(getPlayer());
			new JsonMessage( "      " + C.cGreen + C.Line + _url).click(ClickEvent.OPEN_URL, _url).sendToPlayer(getPlayer());
			UtilPlayer.message(getPlayer(), "");
			UtilPlayer.message(getPlayer(), "      Please be patient. Votes may take a few minutes to register.");
			UtilPlayer.message(getPlayer(), "");
			UtilPlayer.message(getPlayer(), C.cGold + C.Bold + C.Strike + "=============================================");

			getPlayer().closeInventory();
			
		}
		else
		{
			getPlayer().playSound(getPlayer().getLocation(), Sound.ITEM_BREAK, 1, 10);
		}
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

		if (isAvailable() && canUse())
		{
			material = Material.JUKEBOX;
			itemName = C.cGreen + C.Bold + "Vote for Mineplex";

			lore.add(" ");
			lore.add(ChatColor.RESET + "Click to Vote!");
		}
		else if (!canUse())
		{
			material = Material.REDSTONE_BLOCK;
			itemName = C.cRed + C.Bold + "Vote for Mineplex";

			lore.add(" ");
			lore.add(ChatColor.RESET + "You must set your home server by joining a clan to receive voting rewards!");
		}
		else
		{
			material = Material.REDSTONE_BLOCK;
			itemName = C.cRed + C.Bold + "Vote for Mineplex";

			lore.add(" ");
			lore.add(ChatColor.RESET + "Next vote in " + UtilTime.convertString(timeLeft(), 0, TimeUnit.FIT) + "!");
		}

		lore.add(" ");
		
		if (!_bonusManager.ClansBonus)
		{
			BonusClientData client = _bonusManager.Get(_player);

			BonusAmount bonusAmount = _bonusManager.getVoteBonusAmount(_player);
			bonusAmount.addLore(lore);
			lore.add(" ");

			lore.add(C.cYellow + "Current Streak: " + C.cWhite + client.getVoteStreak());
			lore.add(C.cYellow + "Streak Bonus: " + C.cWhite + "+" + _bonusManager.getVoteMultiplier(client.getVoteStreak()) + "%");
			if (client.getVoteTime() != null)
			{
				long lastBonus = _bonusManager.getLocalTime(client.getVoteTime().getTime());
				long timeLeft = _bonusManager.getStreakTimeRemaining(lastBonus, BonusManager.VOTE_STREAK_RESET_TIME);

				if (timeLeft > 0)
				{
					lore.add(C.cYellow + "Streak Reset: " + C.cWhite + UtilTime.convertString(timeLeft, 1, TimeUnit.FIT));
				}
			}

			lore.add(" ");
			lore.add(C.cYellow + "Highest Streak: " + C.cWhite + client.getMaxVoteStreak());
			
//			StreakRecord streakRecord = _bonusManager.getVoteStreak();
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
			BonusAmount amount = _bonusManager.getClansVoteBonusAmount(_player);
			amount.addLore(lore);
		}

		_item =  new ShopItem(material, itemName, lore.toArray(new String[0]), 1, false, false);
	}
	
	public long timeLeft()
	{
		return _bonusManager.nextVoteTime(getPlayer()) - System.currentTimeMillis();
	}
	
	public boolean isAvailable()
	{
		if (_url == null)
			return false;

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