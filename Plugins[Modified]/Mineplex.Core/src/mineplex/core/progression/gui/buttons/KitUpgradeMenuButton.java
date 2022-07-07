package mineplex.core.progression.gui.buttons;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import mineplex.core.progression.gui.guis.KitInformationTrackerMenu;
import mineplex.core.progression.gui.guis.KitMenu;
import mineplex.core.progression.math.Calculations;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * This is the button for upgrades. It'll flash when one is available
 * Displays information regarding upgrades on hover
 */
public class KitUpgradeMenuButton extends KitButton
{

	private static final ItemStack ITEM_STACK = new ItemBuilder(Material.ENCHANTMENT_TABLE)
	  .setTitle(C.cYellow + "Upgrade Level")
	  .setLore(" ", C.cRed + "Upgrades Coming Soon!")
	  .build();

	private ItemStack _item;
	private boolean _flash;
	private BukkitTask _task;

	public KitUpgradeMenuButton(ProgressiveKit kit, Player player, KitProgressionManager plugin)
	{
		super(kit, null, plugin);

		_item = ITEM_STACK;
		//The current upgrade level out of 5 for this kit
		int upgradeLevel = kit.getUpgradeLevel(player.getUniqueId());
		//The players level
		int level = kit.getLevel(player.getUniqueId());
		//What's the next UPGRADE LEVEL (1-5)
		int nextUpgradeLevel = Calculations.getNextUpgradeLevel(level);
		//The 1-100 Level
		int nextUpgradeLevelPlayer = Calculations.getNextUpgradeLevelPlayer(level);
		//The difference between the players current level, and the next upgrade level
		int diff = nextUpgradeLevelPlayer - level;

		int balance = plugin.getDonationManager().Get(player).getBalance(GlobalCurrency.GEM);
		if (kit.crownsEnabled())
		{
			balance = plugin.getDonationManager().getCrowns(player);
		}
		
		//This ONLY flashes if their next upgrade level isn't their same one.
		_flash = (kit.usesXp() ? (Calculations.isUpgradeLevelEligible(level) && (nextUpgradeLevel > upgradeLevel)) : (Calculations.isUpgradeLevelEligibleXpLess(balance) && upgradeLevel < Calculations.getNextUpgradeLevelXpLess(upgradeLevel)));

		ChatColor color = Calculations.getColor(level, nextUpgradeLevelPlayer);

		if (kit.showUpgrades())
		{
			ItemBuilder builder = kit.usesXp() ? lore(new ItemBuilder(Material.ENCHANTMENT_TABLE), upgradeLevel, color, diff) : lore(new ItemBuilder(Material.ENCHANTMENT_TABLE), upgradeLevel);
			builder.setTitle(C.cYellow + "Upgrade Level " + upgradeLevel);
			_item = builder.build();
		}

		if (_flash)
		{
			flash();
		}
	}

	@Override
	public ItemStack getItemStack()
	{
		return _item;
	}

	@Override
	public void setItemStack(ItemStack item)
	{
		_item = item;
	}

	private void flash()
	{
		_task = new BukkitRunnable()
		{

			private ItemStack itemClone = ITEM_STACK.clone();

			private ItemStack AIR = new ItemStack(Material.AIR);

			private boolean resetItem = false;

			@Override
			public void run()
			{
				if (!_flash)
				{
					cancel();
					return;
				}

				if (resetItem)
				{
					setItemStack(itemClone);

					resetItem = false;
					return;
				}

				setItemStack(AIR);

				resetItem = true;
			}
		}.runTaskTimer(getPlugin().getPlugin(), 0L, 10L);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_flash = false;

		if (_task != null)
		{
			_task.cancel();
		}

		KitMenu menu = new KitInformationTrackerMenu(getKit(), getPlugin());
		menu.open(player);
	}

	/**
	 * Will be used later
	 */
	private ItemBuilder lore(ItemBuilder builder, int upgradeLevel, ChatColor color, int diff)
	{
		builder.setLore(" ",
		  ChatColor.WHITE + "Upgrade Level: " + ChatColor.GREEN + upgradeLevel + " out of 5",
		  ChatColor.WHITE + "Next Upgrade Unlocked in: " + color + diff + " level" + (diff == 1 ? "" : "s"),
		  "",
		  ChatColor.WHITE + "Click to view Upgrade and XP tracking");
		return builder;
	}
	
	private ItemBuilder lore(ItemBuilder builder, int upgradeLevel)
	{
		builder.setLore(" ",
		  ChatColor.WHITE + "Upgrade Level: " + ChatColor.GREEN + upgradeLevel + " out of 5",
		  "",
		  ChatColor.WHITE + "Click to view Upgrades");
		return builder;
	}
}