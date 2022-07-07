package mineplex.core.cosmetic.ui.page;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClientManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;

public class TreasurePage extends ShopPageBase<CosmeticManager, CosmeticShop>
{
	/**
	 * This is unfinished, and unused.
	 * TODO
	 */

	private static final int[] ROTATION_SLOTS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 35, 34, 33, 32, 31, 30, 29, 28, 27, 18, 9 };
	private static final List<Integer> CHEST_SLOTS = Arrays.asList(new Integer[] {3 + 9 + 9, 6 + 9 + 9, 2 + 9 + 9, 4 + 9 + 9, 5 + 9 + 9});
	private static final List<ChatColor> CHEST_COLORS = Arrays.asList(new ChatColor[] {ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.BLUE, ChatColor.AQUA, ChatColor.GOLD});

	// Used to animate the gui
	private int _ticks;
	private Random _random;

	// Used for blocks that rotate around the gui
	private short _rotationColorOne = 0;
	private short _rotationColorTwo = 0;
	private boolean _rotationForwardOne = true;
	private boolean _rotationForwardTwo = false;
	private int _currentIndexOne = 4;
	private int _currentIndexTwo = 4;

	// Is the animation done, can the player select a chest?
	public boolean _canSelectChest = false;

	// Queues for Chest Colors and Slots
	private LinkedList<ChatColor> _colors;
	private LinkedList<Integer> _chestSlots;

	public TreasurePage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, mineplex.core.donation.DonationManager donationManager, String name, Player player)
	{
		super(plugin, shop, clientManager, donationManager, name, player, 9 * 4);
		_random = new Random();

		// Shuffle random _colors and chest positions
		_colors = new LinkedList<ChatColor>(CHEST_COLORS);
		_chestSlots = new LinkedList<Integer>(CHEST_SLOTS);
		Collections.shuffle(_colors, _random);
		Collections.shuffle(_chestSlots, _random);
	}

	@Override
	protected void buildPage()
	{
		int treasureCount = getPlugin().getInventoryManager().Get(getPlayer()).getItemCount("Treasure Chest");

		_rotationColorOne = _ticks % 2 == 0 ? ((short) _random.nextInt(15)) : _rotationColorOne;
		_rotationColorTwo = _ticks % 20 == 0 ? ((short) _random.nextInt(15)) : _rotationColorTwo;
		ItemStack borderPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, _canSelectChest ? (short) 7 : (short) 15);

		// Set all the border panes
		for (int row = 0; row < 4; row++)
		{
			if (row == 0 || row == 3)
			{
				for (int column = 0; column < 9; column++)
				{
					setItem(column, row, borderPane);
				}
			}
			else
			{
				setItem(0, row, borderPane);
				setItem(8, row, borderPane);
			}
		}

		if (_ticks <= 21)
		{
			rotateBorderPanes();
		}

		if (_ticks == 0)
		{
			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.ANVIL_USE, 4, 1);
		}
		else if (_ticks == 20)
		{
			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.CHEST_OPEN, 4, 1);
		}
		else if (_ticks >= 30 && _ticks <= 120 && _ticks % 20 == 0)
		{
			ChatColor color = _colors.poll();
			String colorName = color.name().toLowerCase();
			colorName = colorName.substring(0, 1).toUpperCase() + colorName.substring(1);
			String chestName = color + colorName + " Chest";
			String[] lore = new String[] { ChatColor.RESET.toString() + ChatColor.WHITE + "Click to Open" };


			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.NOTE_PLING, 4, 1);
			final int slot = _chestSlots.poll();
			addButton(slot, new ShopItem(Material.CHEST, chestName, lore, 1, false), new IButton()
			{
				public void onClick(Player player, ClickType clickType)
				{
					if (_canSelectChest)
					{
						player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
					}
				}
			});

		}
		else if (_ticks == 140)
		{
			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.LEVEL_UP, 4, 1F);
			ItemStack is = new ItemStack(Material.BOOK);
			ItemMeta meta = is.getItemMeta();
			meta.setDisplayName(ChatColor.RESET.toString() + "Select a Chest");
			is.setItemMeta(meta);

			setItem(9 + 4, is);
			addGlow(9 + 4);

			_canSelectChest = true;
		}

		_ticks++;
	}

	public void rotateBorderPanes()
	{
		ItemStack whitePane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
		ItemStack paneOne = new ItemStack(Material.STAINED_GLASS_PANE, 1, _rotationColorOne);
		ItemStack paneTwo = new ItemStack(Material.STAINED_GLASS_PANE, 1, _rotationColorTwo);

		_currentIndexOne = (_currentIndexOne + (_rotationForwardOne ? 1 : -1)) % ROTATION_SLOTS.length;
		if (_currentIndexOne < 0)
			_currentIndexOne = _currentIndexOne + ROTATION_SLOTS.length;

		_currentIndexTwo = (_currentIndexTwo + (_rotationForwardTwo ? 1 : -1)) % ROTATION_SLOTS.length;
		if (_currentIndexTwo < 0)
			_currentIndexTwo = _currentIndexTwo + ROTATION_SLOTS.length;

		if (_currentIndexOne == _currentIndexTwo)
		{
			setItem(ROTATION_SLOTS[_currentIndexOne], whitePane);
		}
		else
		{
			setItem(ROTATION_SLOTS[_currentIndexOne], paneOne);
			setItem(ROTATION_SLOTS[_currentIndexTwo], paneTwo);
		}
	}

	public void update()
	{
		buildPage();
	}

}
