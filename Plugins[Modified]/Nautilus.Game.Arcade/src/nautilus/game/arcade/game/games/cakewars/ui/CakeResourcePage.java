package nautilus.game.arcade.game.games.cakewars.ui;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.shop.CakeItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeResource;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopItemType;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopModule;

public class CakeResourcePage extends ShopPageBase<ArcadeManager, CakeResourceShop>
{

	private static final ItemStack CLOSE_ITEM = new ItemBuilder(Material.BARRIER)
			.setTitle(C.cRedB + "Close")
			.build();

	final CakeWars _game;
	final CakeResource _resource;
	final List<CakeItem> _items;
	final GameTeam _team;
	private int _multiplier;

	public CakeResourcePage(ArcadeManager plugin, CakeResourceShop shop, Player player, CakeResource resource, List<CakeItem> items)
	{
		this(plugin, shop, player, 45, resource, items);
	}

	public CakeResourcePage(ArcadeManager plugin, CakeResourceShop shop, Player player, int slots, CakeResource resource, List<CakeItem> items)
	{
		super(plugin, shop, plugin.GetClients(), plugin.GetDonation(), resource.getName() + " Shop", player, slots);

		_game = (CakeWars) plugin.GetGame();
		_resource = resource;
		_items = items;
		_team = plugin.GetGame().GetTeam(player);
		_multiplier = 1;
	}

	@Override
	protected void buildPage()
	{
		addCloseButton();

		int slot = 10;

		for (CakeItem item : _items)
		{
			CakeShopResult result = getResultPurchase(item);

			addButton(slot, prepareItem(item, result), new CakeShopButton(item));

			if (++slot % 9 == 8)
			{
				slot += 2;
			}
		}

		buildMulti();
	}

	protected void buildMulti()
	{
		buildMultiItem(39, Material.GOLD_NUGGET, 1);
		buildMultiItem(40, Material.GOLD_INGOT, 3);
		buildMultiItem(41, Material.GOLD_BLOCK, 5);
	}

	private void buildMultiItem(int slot, Material material, int multiplier)
	{
		addButton(slot, new ItemBuilder(material)
				.setTitle(C.cYellow + "x" +  multiplier + C.cGold + " Multiplier")
				.addLore("", "Click to set the item multiplier.", "Allows you to buy in bulk!")
				.setGlow(_multiplier == multiplier)
				.build(), (player, clickType) ->
		{
			if (!Recharge.Instance.use(player, "Change Multipler", 250, false, false))
			{
				return;
			}

			_multiplier = multiplier;
			playAcceptSound(player);
			refresh();
		});
	}

	void addCloseButton()
	{
		addButton(4, CLOSE_ITEM, (player, clickType) -> player.closeInventory());
	}

	CakeShopResult getResultPurchase(CakeItem item)
	{
		CakeShopModule module = _game.getCakeShopModule();
		CakeShopItemType itemType = item.getItemType();
		ItemStack itemStack = _resource.getItemStack();

		if (!itemType.isMultiBuy() && (module.ownsItem(_player, item) || containsLowerTier(_player, item.getItemStack())))
		{
			return CakeShopResult.ALREADY_OWNED;
		}
		else if (itemType.isOnePerTeam() && module.ownsItem(_team, item))
		{
			return CakeShopResult.ONLY_ONE;
		}
		else if (!UtilInv.contains(_player, null, itemStack.getType(), itemStack.getData().getData(), getCost(item), false, false, false))
		{
			return CakeShopResult.NOT_ENOUGH_RESOURCES;
		}

		return CakeShopResult.SUCCESSFUL;
	}

	private boolean containsLowerTier(Player player, ItemStack itemStack)
	{
		PlayerInventory inventory = player.getInventory();

		switch (itemStack.getType())
		{
			case IRON_HELMET:
				return inventory.getHelmet() != null && inventory.getHelmet().getType() == Material.DIAMOND_HELMET;
			case IRON_CHESTPLATE:
				return inventory.getChestplate() != null && inventory.getChestplate().getType() == Material.DIAMOND_CHESTPLATE;
			case IRON_LEGGINGS:
				return inventory.getLeggings() != null && inventory.getLeggings().getType() == Material.DIAMOND_LEGGINGS;
			case IRON_BOOTS:
				return inventory.getBoots() != null && inventory.getBoots().getType() == Material.DIAMOND_BOOTS;
			case IRON_SWORD:
				return inventory.contains(Material.DIAMOND_SWORD);
			case IRON_PICKAXE:
				return inventory.contains(Material.DIAMOND_PICKAXE);
		}

		return false;
	}

	ItemStack prepareItem(CakeItem item, CakeShopResult result)
	{
		ItemStack itemStack = item.getItemStack();
		ItemBuilder builder = new ItemBuilder(itemStack);

		handleTeamColours(builder);

		builder.setTitle(getItemName(itemStack));
		builder.addLore("");

		if (shouldScale(item))
		{
			int newAmount = itemStack.getAmount() * _multiplier;
			builder.setAmount(newAmount);

			if (_multiplier > 1)
			{
				builder.addLore("Amount: " + _resource.getChatColor() + newAmount);
			}
		}

		builder.addLore(
				"Cost: " + _resource.getChatColor() + getCost(item) + " " + _resource.getName() + "s",
				"",
				result.getColour() + result.getFeedback()
		);

		return builder.build();
	}

	private void handleTeamColours(ItemBuilder builder)
	{
		Material material = builder.getType();

		if (material == Material.WOOL || material == Material.STAINED_CLAY || material == Material.STAINED_GLASS)
		{
			builder.setData(_team.GetColorData());
		}
		else if (material == Material.INK_SACK)
		{
			builder.setData(_team.getDyeColor().getDyeData());
		}
	}

	private String getItemName(ItemStack itemStack)
	{
		if (itemStack.getType() == Material.STAINED_CLAY)
		{
			return C.mItem + "Stained Clay";
		}
		if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
		{
			return itemStack.getItemMeta().getDisplayName();
		}

		return ItemStackFactory.Instance.GetName(itemStack, true);
	}

	private int getCost(CakeItem item)
	{
		return shouldScale(item) ? item.getCost() * _multiplier : item.getCost();
	}

	private boolean shouldScale(CakeItem item)
	{
		return item.getItemType().isMultiBuy();
	}

	protected enum CakeShopResult
	{

		SUCCESSFUL(ChatColor.GREEN, "Click to purchase!"),
		NOT_ENOUGH_RESOURCES(ChatColor.RED, "You do not have enough resources!"),
		ALREADY_OWNED(ChatColor.RED, "You already have purchased this item."),
		ONLY_ONE(ChatColor.RED, "Your team already owns this upgrade."),
		MAX_TIER(ChatColor.RED, "Your team has already unlocked the maximum tier.");

		private final ChatColor _colour;
		private final String _feedback;

		CakeShopResult(ChatColor colour, String feedback)
		{
			_colour = colour;
			_feedback = feedback;
		}

		public ChatColor getColour()
		{
			return _colour;
		}

		public String getFeedback()
		{
			return _feedback;
		}
	}

	private class CakeShopButton implements IButton
	{

		private final CakeItem _item;

		CakeShopButton(CakeItem item)
		{
			_item = item;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			if (!Recharge.Instance.use(player, "Buy Item", 250, false, false))
			{
				return;
			}

			// It's important to re-get the result otherwise it's possible to do a very
			// quick but difficult dupe bug. Involving opening the ui and dropping the items
			// at the same time.
			CakeShopResult result = getResultPurchase(_item);

			if (result != CakeShopResult.SUCCESSFUL)
			{
				player.sendMessage(F.main("Game", result.getFeedback()));
				playDenySound(player);
				return;
			}

			CakeShopItemType itemType = _item.getItemType();
			ItemStack resource = _resource.getItemStack();
			ItemStack itemStack = _item.getItemStack();
			ItemBuilder give = new ItemBuilder(itemStack);

			if (!shouldScale(_item))
			{
				give.setUnbreakable(true);
			}
			else
			{
				give.setAmount(itemStack.getAmount() * _multiplier);
			}

			UtilInv.remove(player, resource.getType(), resource.getData().getData(), getCost(_item));
			handleTeamColours(give);

			ItemStack giveItem = give.build();
			PlayerInventory inventory = player.getInventory();

			if (itemType.getRemoveOnPurchase() != null)
			{
				for (int i = 0; i < inventory.getSize(); i++)
				{
					if (UtilItem.is(inventory.getItem(i), itemType.getRemoveOnPurchase()))
					{
						inventory.setItem(i, null);
					}
				}
			}

			switch (itemType)
			{
				case HELMET:
					inventory.setHelmet(giveItem);
					break;
				case CHESTPLATE:
					inventory.setChestplate(giveItem);
					break;
				case LEGGINGS:
					inventory.setLeggings(giveItem);
					break;
				case BOOTS:
					inventory.setBoots(giveItem);
					break;
				default:
					if (itemType.isItem())
					{
						UtilInv.insert(player, giveItem);
					}
					break;
			}

			Set<CakeItem> ownedItems = _game.getCakeShopModule().getOwnedItems(player);

			if (!itemType.isMultiBuy())
			{
				ownedItems.removeIf(item -> item.getItemType().equals(itemType));
			}

			if (itemType.isOnePerTeam())
			{
				_game.getCakeShopModule().getOwnedItems(_team).add(_item);
			}

			ownedItems.add(_item);

			_game.getArcadeManager().getMissionsManager().incrementProgress(player, _item.getCost(), MissionTrackerType.CW_SPEND_RESOURCE, _game.GetType().getDisplay(), _resource.getChatColor());

			player.sendMessage(F.main("Game", "You purchased " + F.name(getItemName(giveItem))) + ".");
			playAcceptSound(player);
			getShop().getPageMap().values().forEach(page ->
			{
				if (page.getName().equals(getName()))
				{
					page.refresh();
				}
			});
		}
	}
}
