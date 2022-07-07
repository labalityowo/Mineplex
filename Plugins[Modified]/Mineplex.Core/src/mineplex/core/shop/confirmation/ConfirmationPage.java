package mineplex.core.shop.confirmation;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @author Shaun Bennett
 */
public class ConfirmationPage<PluginType extends MiniPlugin, ShopType extends ShopBase<PluginType>> extends ShopPageBase<PluginType, ShopType> implements Runnable, ConfirmationCallback
{
	private int _taskId;

	private ShopPageBase<PluginType, ShopType> _returnPage;
	private ItemStack _displayItem;

	private ItemStack _progressItem = new ShopItem(Material.LAPIS_BLOCK, (byte)11, ChatColor.BLUE + "Processing", null, 1, false, true);
	private int _okSquareSlotStart = 27;
	private int _progressCount;
	private ConfirmationProcessor _processor;
	private boolean _processing;

	public ConfirmationPage(Player player, ShopPageBase<PluginType, ShopType> returnPage, ConfirmationProcessor processor, ItemStack displayItem, String name)
	{
		super(returnPage.getPlugin(), returnPage.getShop(), returnPage.getClientManager(), returnPage.getDonationManager(), name, player);

		_returnPage = returnPage;
		_displayItem = displayItem;
		_processor = processor;

		buildPage();
	}

	public ConfirmationPage(Player player, ShopPageBase<PluginType, ShopType> returnPage, ConfirmationProcessor processor, ItemStack displayItem)
	{
		this(player, returnPage, processor, displayItem, "Confirmation");
	}

	public ConfirmationPage(Player player, PluginType plugin, ShopType shop, CoreClientManager clientManager, DonationManager donationManager, ConfirmationProcessor processor, ItemStack displayItem)
	{
		super(plugin, shop, clientManager, donationManager, "Confirmation", player);

		_displayItem = displayItem;
		_processor = processor;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		setItem(22, _displayItem);

		buildSquareAt(_okSquareSlotStart, new ShopItem(Material.EMERALD_BLOCK, (byte) 0, ChatColor.GREEN + "OK", null, 1, false, true), this::okClicked);
		buildSquareAt(_okSquareSlotStart + 6, new ShopItem(Material.REDSTONE_BLOCK, (byte) 0, ChatColor.RED + "CANCEL", null, 1, false, true), this::cancelClicked);

		_processor.init(this);
	}

	protected void okClicked(Player player, ClickType clickType)
	{
		processTransaction();
	}

	protected void cancelClicked(Player player, ClickType clickType)
	{
		getPlugin().getScheduler().cancelTask(_taskId);

		if (_returnPage != null)
		{
			getShop().openPageForPlayer(player, _returnPage);
		}
		else
		{
			player.closeInventory();
		}

	}

	private void buildSquareAt(int slot, ShopItem item, IButton button)
	{
		addButton(slot, item, button);
		addButton(slot + 1, item, button);
		addButton(slot + 2, item, button);

		slot += 9;

		addButton(slot, item, button);
		addButton(slot + 1, item, button);
		addButton(slot + 2, item, button);

		slot += 9;

		addButton(slot, item, button);
		addButton(slot + 1, item, button);
		addButton(slot + 2, item, button);
	}

	private void processTransaction()
	{
		for (int i=_okSquareSlotStart; i < 54; i++)
		{
			getButtonMap().remove(i);
			clear(i);
		}

		_processing = true;

		_processor.process(this);

		_taskId = getPlugin().getScheduler().scheduleSyncRepeatingTask(getPlugin().getPlugin(), this, 2L, 2L);
	}

	private void buildErrorPage(String... message)
	{
		ShopItem item = new ShopItem(Material.REDSTONE_BLOCK, (byte)0, ChatColor.RED + "" + ChatColor.UNDERLINE + "ERROR", message, 1, false, true);
		for (int i = 0; i < this.getSize(); i++)
		{
			addButton(i, item, this::cancelClicked);
		}

		getPlayer().playSound(getPlayer().getLocation(), Sound.BLAZE_DEATH, 1, .1f);
	}

	private void buildSuccessPage(String message)
	{
		ShopItem item = new ShopItem(Material.EMERALD_BLOCK, (byte)0, ChatColor.GREEN + message, null, 1, false, true);
		for (int i = 0; i < this.getSize(); i++)
		{
			addButton(i, item, this::cancelClicked);
		}

		getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1, .9f);
	}

	@Override
	public void playerClosed()
	{
		super.playerClosed();

		Bukkit.getScheduler().cancelTask(_taskId);

		if (_returnPage != null && getShop() != null)
		{
			getShop().setCurrentPageForPlayer(getPlayer(), _returnPage);
		}
	}

	@Override
	public void run()
	{
		if (_processing)
		{
			if (_progressCount == 9)
			{
				for (int i=45; i < 54; i++)
				{
					clear(i);
				}

				_progressCount = 0;
			}

			setItem(45 + _progressCount, _progressItem);
		}
		else
		{
			if (_progressCount >= 20)
			{
				try
				{
					Bukkit.getScheduler().cancelTask(_taskId);

					if (_returnPage != null && getShop() != null)
					{
						getShop().openPageForPlayer(getPlayer(), _returnPage);
					}
					else if (getPlayer() != null)
					{
						getPlayer().closeInventory();
					}
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
				finally
				{
					dispose();
				}
			}
		}

		_progressCount++;
	}

	@Override
	public void dispose()
	{
		super.dispose();

		Bukkit.getScheduler().cancelTask(_taskId);
	}

	@Override
	public void resolve(String message)
	{
		_processing = false;
		buildSuccessPage(message);
		_progressCount = 0;
	}

	@Override
	public void reject(String message)
	{
		_processing = false;
		buildErrorPage(message);
		_progressCount = 0;
	}
}