package mineplex.core.cosmetic.ui.page;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.button.GadgetButton;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.event.GadgetChangeEvent;
import mineplex.core.gadget.event.GadgetChangeEvent.GadgetState;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.item.SalesPackageProcessor;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;

public class GadgetPage extends ShopPageBase<CosmeticManager, CosmeticShop>
{

	private static final int ELEMENTS_PER_PAGE = 28;

	private final GadgetType _gadgetType;

	private int _page;

	public GadgetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player)
	{
		this(plugin, shop, clientManager, donationManager, name, player, null);
	}

	public GadgetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, GadgetType gadgetType)
	{
		super(plugin, shop, clientManager, donationManager, name, player);

		_gadgetType = gadgetType;
	}

	@Override
	protected void buildPage()
	{
		List<Gadget> gadgets = getGadgetsToDisplay();

		if (gadgets == null)
		{
			return;
		}

		int slot = 10;
		int startIndex = _page * ELEMENTS_PER_PAGE;
		int endIndex = startIndex + ELEMENTS_PER_PAGE;

		gadgets = gadgets.subList(startIndex, Math.min(endIndex, gadgets.size()));

		for (Gadget gadget : gadgets)
		{
			if (gadget.isHidden())
			{
				continue;
			}

			addGadget(gadget, slot);

			if (gadget.isActive(getPlayer()))
			{
				addGlow(slot);
			}

			if (++slot % 9 == 8)
			{
				slot += 2;
			}
		}

		if (_page != 0)
		{
			addButton(45, new ShopItem(Material.ARROW, C.cGreen + "Previous Page", new String[0], 1, false), (player, clickType) ->
			{
				_page--;
				refresh();
			});
		}
		if (endIndex <= gadgets.size())
		{
			addButton(53, new ShopItem(Material.ARROW, C.cGreen + "Next Page", new String[0], 1, false), (player, clickType) ->
			{
				_page++;
				refresh();
			});
		}

		addBackButton();
	}

	protected List<Gadget> getGadgetsToDisplay()
	{
		return getPlugin().getGadgetManager().getGadgets(_gadgetType);
	}

	protected void addBackButton()
	{
		addBackButton(4);
	}

	protected void addBackButton(int slot)
	{
		addButton(slot, new ShopItem(Material.BED, C.cGreen + "Go Back", new String[0], 1, false), (player, clickType) -> getShop().openPageForPlayer(getPlayer(), new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player)));
	}

	protected void addGadget(Gadget gadget, int slot)
	{
		boolean owns = gadget.ownsGadget(getPlayer());
		int shardCost = gadget.getCost(GlobalCurrency.TREASURE_SHARD);
		List<String> itemLore = new ArrayList<>();

		itemLore.add(C.cBlack);
		itemLore.addAll(Arrays.asList(gadget.getDescription()));

		GadgetSet set = gadget.getSet();
		if (set != null)
		{
			itemLore.add(C.cBlack);
			itemLore.add(C.cWhite + set.getName() + " Set");

			//Elements
			for (Gadget cur : gadget.getSet().getGadgets())
			{
				itemLore.add("   " + (cur.ownsGadget(getPlayer()) ? C.cDGreen : C.cDRed) + "▪ " + (cur.isActive(getPlayer()) ? C.cGreen : C.cGray) + cur.getName());
			}

			itemLore.add(C.cBlack);

			//Bonus
			itemLore.add(C.cWhite + set.getName() + " Set Bonus");
			for (String bonus : set.getBonus())
			{
				itemLore.add("   " + (set.isActive(getPlayer()) ? C.cGreen : C.cGray) + bonus);
			}
		}

		if (!owns)
		{
			int displayedShardCost = shardCost;

			if (displayedShardCost > 0)
			{
				displayedShardCost = CostConstants.FOUND_IN_TREASURE_CHESTS;
			}

			switch (displayedShardCost)
			{

				// Nothing
				case CostConstants.NO_LORE:
					break;

				// Chests
				case CostConstants.FOUND_IN_TREASURE_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Treasure Chests");
					break;

				case CostConstants.FOUND_IN_WINTER_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Winter Holiday Treasure");
					break;

				case CostConstants.FOUND_IN_HALLOWEEN_PUMPKIN_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Halloween Pumpkin Treasure");
					break;

				case CostConstants.FOUND_IN_EASTER_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Easter Holiday Treasure");
					break;

				case CostConstants.FOUND_IN_VALENTINES_GIFTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Valentines Gifts");
					break;

				case CostConstants.FOUND_IN_FREEDOM_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Freedom Chests");
					break;

				case CostConstants.FOUND_IN_HAUNTED_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Haunted Chests");
					break;

				case CostConstants.FOUND_IN_GINGERBREAD_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Gingerbread Chests");
					break;

				case CostConstants.FOUND_IN_LOVE_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Love Chests");
					break;

				case CostConstants.FOUND_IN_ST_PATRICKS_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in St Patrick's Chests");
					break;

				case CostConstants.FOUND_IN_SPRING_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Spring Chests");
					break;

				case CostConstants.FOUND_IN_MOBA_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in HOG Chests");
					break;

				case CostConstants.FOUND_IN_THANKFUL_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Thankful Treasure");
					break;

				case CostConstants.FOUND_IN_TRICK_OR_TREAT:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in Trick or Treat Treasure");
					break;

				case CostConstants.FOUND_IN_MINESTRIKE_CHESTS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Found in MineStrike Chests");
					break;


				// Ranks
				case CostConstants.UNLOCKED_WITH_ULTRA:
					itemLore.add(C.cBlack);
					itemLore.add(C.cAqua + "Unlocked with Ultra Rank");
					break;

				case CostConstants.UNLOCKED_WITH_HERO:
					itemLore.add(C.cBlack);
					itemLore.add(C.cPurple + "Unlocked with Hero Rank");
					break;

				case CostConstants.UNLOCKED_WITH_LEGEND:
					itemLore.add(C.cBlack);
					itemLore.add(C.cGreen + "Unlocked with Legend Rank");
					break;

				case CostConstants.UNLOCKED_WITH_TITAN:
					itemLore.add(C.cBlack);
					itemLore.add(C.cRed + "Unlocked with Titan Rank");
					break;

				case CostConstants.UNLOCKED_WITH_ETERNAL:
					itemLore.add(C.cBlack);
					itemLore.add(C.cDAqua + "Unlocked with Eternal Rank");
					break;

				// Special
				case CostConstants.POWERPLAY_BONUS:
					itemLore.add(C.cBlack);

					YearMonth yearMonth = gadget.getYearMonth();
					if (yearMonth != null)
					{
						int year = yearMonth.getYear();
						Month month = yearMonth.getMonth();
						String monthName = month.getDisplayName(TextStyle.FULL, Locale.US);
						itemLore.addAll(UtilText.splitLine(C.cBlue + "Monthly Power Play Club Reward for " + monthName + " " + year, LineFormat.LORE));
					}
					else
					{
						itemLore.add(C.cBlue + "Bonus Item Unlocked with Power Play Club");
					}

					break;

				case CostConstants.PURCHASED_FROM_STORE:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Purchased from mineplex.com/shop");
					break;

				case CostConstants.LEVEL_REWARDS:
					itemLore.add(C.cBlack);
					itemLore.add(C.cBlue + "Unlocked in Level Rewards");
					break;

				default:
					break;
			}
		}

		addCustomLore(gadget, itemLore);

		if (owns)
		{
			ItemStack gadgetItemStack;

			if (gadget.hasDisplayItem())
			{
				gadgetItemStack = gadget.getDisplayItem();
			}
			else
			{
				gadgetItemStack = new ItemStack(gadget.getDisplayMaterial(), 1, gadget.getDisplayData());
			}

			ItemMeta meta = gadgetItemStack.getItemMeta();
			meta.setDisplayName(C.cGreenB + gadget.getName());
			itemLore.add(C.cBlack);

			if (gadget.isActive(getPlayer()))
			{
				itemLore.add(C.cGreen + "Left-Click to Disable");
			}
			else
			{
				itemLore.add(C.cGreen + "Left-Click to Enable");
			}

			if (set != null)
			{
				if (set.isActive(getPlayer()))
				{
					itemLore.add(C.cGreen + "Shift-Click to Disable Set");
				}
				else
				{
					itemLore.add(C.cGreen + "Shift-Click to Enable Set");
				}
			}

			meta.setLore(itemLore);
			gadgetItemStack.setItemMeta(meta);

			addButton(slot, new ShopItem(gadgetItemStack, false, false).hideInfo(), (player, clickType) ->
			{
				if (clickType.isShiftClick())
				{
					toggleSet(player, gadget);
				}
				else
				{
					toggleGadget(player, gadget);
				}
			});
		}
		else
		{
			if (shardCost > 0)
			{
				itemLore.add(C.cBlack);
				itemLore.add(C.cWhiteB + "Cost: " + C.cAqua + gadget.getCost(GlobalCurrency.TREASURE_SHARD) + " Treasure Shards");

				if (getDonationManager().Get(getPlayer()).getBalance(GlobalCurrency.TREASURE_SHARD) >= shardCost)
				{
					itemLore.add(C.cBlack);
					itemLore.add(C.cGreen + "Click to Purchase");

					addButton(slot, new ShopItem(Material.INK_SACK, (byte) 8, gadget.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false).hideInfo(), new GadgetButton(gadget, this));
				}
				else
				{
					itemLore.add(C.cBlack);
					itemLore.add(C.cRed + "Not enough Treasure Shards.");

					setItem(slot, new ShopItem(Material.INK_SACK, (byte) 8, gadget.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false).hideInfo());
				}
			}
			else
			{
				setItem(slot, new ShopItem(Material.INK_SACK, (byte) 8, gadget.getName(), itemLore.toArray(new String[itemLore.size()]), 1, true, false).hideInfo());
			}
		}
	}

	protected void addCustomLore(Gadget gadget, List<String> lore)
	{
	}

	public void purchaseGadget(final Player player, final Gadget gadget)
	{
		getShop().openPageForPlayer(
				getPlayer(),
				new ConfirmationPage<>(
						player,
						this,
						new SalesPackageProcessor(
								player,
								GlobalCurrency.TREASURE_SHARD,
								gadget,
								getDonationManager(),
								() ->
								{
									getPlugin().getInventoryManager().addItemToInventory(getPlayer(), gadget.getName(), gadget.getQuantity());
									refresh();
								}
						),
						gadget.buildIcon()
				)
		);
	}

	protected void toggleGadget(Player player, Gadget gadget)
	{
		playAcceptSound(player);

		if (gadget.isActive(player))
		{
			deactivateGadget(player, gadget);
		}
		else
		{
			activateGadget(player, gadget);
		}
	}

	protected void toggleSet(Player player, Gadget gadget)
	{
		GadgetSet set = gadget.getSet();

		if (set == null)
		{
			return;
		}

		if (set.isActive(player))
		{
			for (Gadget setGadget : set.getGadgets())
			{
				deactivateGadget(player, setGadget);
			}

			playAcceptSound(player);
		}
		else
		{
			for (Gadget setGadget : set.getGadgets())
			{
				if (setGadget.ownsGadget(player))
				{
					activateGadget(player, setGadget);
				}
			}
		}
	}

	private void activateGadget(Player player, Gadget gadget)
	{
		gadget.enable(player);
		UtilServer.CallEvent(new GadgetChangeEvent(player, gadget, GadgetState.ENABLED));
		refresh();
	}

	private void deactivateGadget(Player player, Gadget gadget)
	{
		gadget.disable(player);
		UtilServer.CallEvent(new GadgetChangeEvent(player, gadget, GadgetState.DISABLED));
		refresh();
	}
}