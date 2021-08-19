package mineplex.core.progression.gui.guis;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.IconButton;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import mineplex.core.progression.gui.buttons.KitUpgradeButton;
import mineplex.core.progression.gui.buttons.KitUpgradeDetailsButton;
import mineplex.core.progression.gui.buttons.misc.BackButton;
import mineplex.core.progression.math.Calculations;

/**
 * This is the secondary menu access by either the Enchantment table or XP Bottle
 * Displays the more technical data of a kit for the player
 */
public class KitInformationTrackerMenu extends KitMenu
{

	private final DyeColor[] COLORS = {
	  DyeColor.GRAY,
	  DyeColor.YELLOW,
	  DyeColor.ORANGE,
	  DyeColor.LIME,
	  DyeColor.BLACK
	};

	private final int[] XP_SLOTS = {
	  11, 12, 13, 14, 15
	};
	private final int[] LEVEL_SLOTS = {
	  29, 30, 31, 32, 33
	};
	private final int[] UPGRADE_SLOTS = {
	  47, 48, 49, 50, 51
	};

	public KitInformationTrackerMenu(ProgressiveKit kit, KitProgressionManager plugin)
	{
		super(kit, plugin);
	}

	@Override
	public Button[] setUp(Player player)
	{
		Button[] buttons = new Button[52];

		buttons[0] = new BackButton(new KitDisplayMenu(getKit(), getPlugin()));
		
		if (getKit().usesXp())
		{
			setUpXP(buttons, player);
			setUpLevel(buttons, player);
		}
		
		setUpUpgrade(buttons, player);

		return buttons;
	}

	/**
	 * Set up the row of glass panes symbolizing the players XP advancement
	 *
	 * @param buttons The array of buttons we're modifying
	 * @param player  The player whose data we'll be using
	 */
	private void setUpXP(Button[] buttons, Player player)
	{
		ProgressiveKit kit = getKit();

		UUID uuid = player.getUniqueId();

		int level = kit.getLevel(uuid);
		int xp = kit.getXp(uuid);
		int nextXp = Calculations.getXpForNextLevel(level);

		float perc = (xp * 100.0f) / nextXp;

		String[] lore = {
		  " ",
		  ChatColor.WHITE + "Current XP: " + Calculations.getColor(xp, nextXp) + xp +
			ChatColor.GRAY + " (" + Calculations.getColor(xp, nextXp) + (int) perc + "%" + ChatColor.GRAY + ")",
		  xpLore(getKit(), player)
		};

		for (int i : XP_SLOTS)
		{
			buttons[i] = new IconButton(new ItemBuilder(Material.STAINED_GLASS_PANE)
			  .setData(DyeColor.BLACK.getWoolData())
			  .setAmount(1)
			  .setTitle(ChatColor.YELLOW + "Experience Progression")
			  .setLore(lore)
			  .build());
		}

		percentButtons(true, perc, XP_SLOTS, buttons, xp, nextXp, player);
	}

	/**
	 * Set up the row of glass panes symbolizing the players level advancement
	 *
	 * @param buttons The array of buttons we're modifying
	 * @param player  The player whose data we'll be using
	 */
	private void setUpLevel(Button[] buttons, Player player)
	{
		ProgressiveKit kit = getKit();

		UUID uuid = player.getUniqueId();
		int level = kit.getLevel(uuid);

		float perc = (level * 100.0f) / 100;

		String[] lore = {
		  " ",
		  ChatColor.WHITE + "Current Level: " + Calculations.getColor(level, 100) + level +
			ChatColor.GRAY + " (" + Calculations.getColor(level, 100) + level +
			"/" + ChatColor.GREEN + 100 + ChatColor.GRAY + ")"
		};

		for (int i : LEVEL_SLOTS)
		{
			buttons[i] = new IconButton(new ItemBuilder(Material.STAINED_GLASS_PANE)
			  .setData(DyeColor.BLACK.getWoolData())
			  .setAmount(1)
			  .setTitle(ChatColor.YELLOW + "Level Progression")
			  .setLore(lore)
			  .build());
		}

		percentButtons(false, perc, LEVEL_SLOTS, buttons, level, 100, player);
	}

	/**
	 * Set up the row of items symbolizing the players upgrade advancement
	 *
	 * @param buttons The array of buttons we're modifying
	 * @param player  The player whose data we'll be using
	 */
	private void setUpUpgrade(Button[] buttons, Player player)
	{
		if(getKit().showUpgrades())
		{
			Map<Integer, List<String>> details = getKit().getUpgradeDetails();

			int index = 0;
			for (int i : (getKit().usesXp() ? UPGRADE_SLOTS : LEVEL_SLOTS))
			{
				List<String> list = details.get(index++);
				KitUpgradeDetailsButton detailsButton = new KitUpgradeDetailsButton(getKit(), player, index, list);
				ItemBuilder itemStack = new ItemBuilder(detailsButton.getItemStack());

				if (getKit().ownsUpgrade(player.getUniqueId(), index))
				{
					itemStack.addLore(C.cRed + "You already own this upgrade!");
				}
				else if (getKit().canPurchaseUpgrade(player.getUniqueId(), index))
				{
					itemStack.setGlow(true);
					if (getKit().usesXp())
					{
						itemStack.addLore("Costs " + (getKit().crownsEnabled() ? C.cGold : C.cGreen) + Calculations.getGemsCost(index) + C.cGray + (getKit().crownsEnabled() ? " crowns" : " gems"));
					}
					itemStack.addLore(C.cGreen + "Click to purchase this upgrade!");
				}
				else
				{
					if (getKit().usesXp())
					{
						itemStack.addLore("Costs " + (getKit().crownsEnabled() ? C.cGold : C.cGreen) + Calculations.getGemsCost(index) + C.cGray + (getKit().crownsEnabled() ? " crowns" : " gems"));
					}
					itemStack.addLore(C.cRed + "You cannot purchase this upgrade!");
				}
				
				KitUpgradeButton upgradeButton = new KitUpgradeButton(getPlugin(), getKit(), itemStack.build(), index);
			 	buttons[i] = upgradeButton;
			}
			return;
		}

		for (int i : UPGRADE_SLOTS)
		{
			buttons[i] = new IconButton(COMING_SOON);
		}
	}

	/* ======================================================================================================
	  *
	  *
	  * Below here are utility methods that speed up the process for me, and makes the code neater and easier
	  * to read.
	  *
	  * ====================================================================================================== */

	@SuppressWarnings("Duplicates")
	private int getLocked(int level)
	{
		switch (level)
		{
			case 1:
				return 5;
			case 2:
				return 10;
			case 3:
				return 30;
			case 4:
				return 75;
			case 5:
				return 100;
		}
		if (level >= 5)
		{
			return 100;
		}
		return -1;
	}

	private void percentButtons(boolean xp, float perc, int[] array, Button[] buttons, int number, int total, Player player)
	{
		float checkAgainst = perc;

		if (perc <= 20)
		{
			//One button
			buttons[array[0]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			return;
		}

		if (perc <= 40)
		{
			buttons[array[0]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			checkAgainst -= 20;
			buttons[array[1]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			return;
		}

		if (perc <= 60)
		{
			buttons[array[0]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			buttons[array[1]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			checkAgainst -= 40;
			buttons[array[2]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			return;
		}
		if (perc <= 80)
		{
			buttons[array[0]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			buttons[array[1]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			buttons[array[2]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			checkAgainst -= 60;
			buttons[array[3]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
			return;
		}
		buttons[array[0]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
		buttons[array[1]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
		buttons[array[2]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
		buttons[array[3]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
		checkAgainst -= 80;
		buttons[array[4]] = new IconButton(paneIcon(xp, number, total, perc, checkAgainst, player));
	}

	private ItemStack paneIcon(boolean xp, int number, int total, float perc, float checkAgainst, Player player)
	{
		String title = ChatColor.YELLOW + (xp ? "Experience Progression" : "Level Progression");
		String current = ChatColor.WHITE + "Current " + (xp ? "XP" : "Level") + ": " + Calculations.getColor(number, total) + number;
		String calculatedPerc = ChatColor.GRAY + " (" + Calculations.getColor(number, total) + (int) perc + "%" + ChatColor.GRAY + ")";

		List<String> loreList = Lists.newArrayList(" ");
		if (xp)
		{
			loreList.add(current + calculatedPerc);
			loreList.add(xpLore(getKit(), player));
		} else
		{
			loreList.add(current + ChatColor.GRAY + " (" + Calculations.getColor(number, total) + number +
			  "/" + ChatColor.GREEN + total + ChatColor.GRAY + ")");
		}

		return new ItemBuilder(Material.STAINED_GLASS_PANE)
		  .setData(perc >= 20 ? DyeColor.LIME.getWoolData() : getColor(checkAgainst))
		  .setTitle(title)
		  .setLore(loreList.toArray(new String[loreList.size()]))
		  .build();
	}


	private byte getColor(float perc)
	{
		if (perc <= 5)
		{
			return COLORS[0].getWoolData();
		}

		if (perc <= 10)
		{
			return COLORS[1].getWoolData();
		}

		if (perc <= 15)
		{
			return COLORS[2].getWoolData();
		}

		if (perc <= 20)
		{
			return COLORS[3].getWoolData();
		}

		return COLORS[4].getWoolData();
	}

	private String xpLore(ProgressiveKit kit, Player player)
	{
		StringBuilder xp = new StringBuilder(C.cWhite + "Progress: ");

		int xpInt = kit.getXp(player.getUniqueId());
		int xpNext = Calculations.getXpForNextLevel(kit.getLevel(player.getUniqueId()));
		int diff = kit.getXpDifference(player.getUniqueId());

		xp.append(Calculations.getColor(xpInt, xpNext)).append(xpInt)
		  .append(ChatColor.GRAY).append("/")
		  .append(ChatColor.GREEN).append(xpNext)
		  .append(ChatColor.GRAY).append(" (")
		  .append(Calculations.getColor(xpInt, xpNext)).append(diff)
		  .append(" needed")
		  .append(ChatColor.GRAY).append(")");
		return xp.toString();
	}



}
