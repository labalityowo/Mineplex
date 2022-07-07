package mineplex.staffServer.ui.chest;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.MultiPageManager;
import mineplex.core.treasure.types.TreasureType;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportChestPage extends SupportPage
{
	private Map<TreasureType, Integer> _treasurePurchasedShop;
	private Map<TreasureType, Integer> _treasurePurchasedShards;
	private MultiPageManager<TreasureType> _multiPageManager;

	public SupportChestPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage homePage)
	{
		super(plugin, shop, player, target, homePage, "Chests");

		_treasurePurchasedShop = new LinkedHashMap<>();
		_treasurePurchasedShards = new LinkedHashMap<>();
		_multiPageManager = new MultiPageManager<>(this, this::getTypesList, this::buildTreasure);

		processReceivedTreasures();

		buildPage();
	}

	public ItemStack getTreasureItem(TreasureType type, List<String> lore)
	{
		ItemStack item = type.getItemStack().clone();

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(C.cGreenB + type.getItemName());
		meta.setLore(lore);

		item.setItemMeta(meta);

		return item;
	}

	private void processReceivedTreasures()
	{
		Map<String, TreasureType> treasureNameToType = new LinkedHashMap<>();

		for (TreasureType type : TreasureType.values())
		{
			treasureNameToType.put(type.getItemName(), type);
		}

		Map<String, Integer> shopPurchases = getPackageOwnership(new LinkedList<>(treasureNameToType.keySet()));
		Map<String, Integer> shardPurchases = getPackageOwnership(new LinkedList<>(treasureNameToType.keySet()), (transactionToken) -> transactionToken.Coins != 0);

		shopPurchases.forEach((treasureName, count) -> _treasurePurchasedShop.put(treasureNameToType.get(treasureName), count));
		shardPurchases.forEach((treasureName, count) -> _treasurePurchasedShards.put(treasureNameToType.get(treasureName), count));
	}

	private void buildTreasure(TreasureType type, int slot)
	{
		int shopCount = _treasurePurchasedShop.computeIfAbsent(type, t -> 0);
		int shardCount = _treasurePurchasedShards.computeIfAbsent(type, t -> 0);

		ItemStack item = getTreasureItem(type, Arrays.asList(
				C.cGray + "Currently Owned: " + C.cYellow + getOwnedCount(type.getItemName()),
				"",
				C.Reset + "Received from:",
				C.cGray + "Shop: " + C.cYellow + shopCount,
				C.cGray + "Shards: " + C.cYellow + shardCount,
				"",
				C.cGreen + "Click to give chests",
				C.cGreen + "of this type"));

		item.setAmount(1);

		addButton(slot, item, (p, c)-> getShop().openPageForPlayer(getPlayer(), new SupportGiveChestPage(getPlugin(), getShop(), getPlayer(), _target, this, type)));
	}

	private LinkedList<TreasureType> getTypesList()
	{
		return Arrays.stream(TreasureType.values())
				// If there's no itemstack it's not a
				// treasure chest
				.filter(t -> t.getItemStack() != null)
				.collect(Collectors.toCollection(LinkedList::new));
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		_multiPageManager.buildPage();

		addItem(getSlotIndex(5, 3),
				new ItemBuilder(Material.SKULL_ITEM)
						.setData(UtilSkull.getSkullData(EntityType.CREEPER))
						.setTitle(C.cGreenB + "Carl Spinner Tickets")
						.addLore(C.mBody + "Amount Owned: " + C.cYellow + getShop().getBonusData().get(_target.getAccountId()).getTickets())
						.build());

		addItem(getSlotIndex(5, 5),
				new ItemBuilder(Material.GOLD_INGOT)
					.setTitle(C.cGreenB + "Davy Jones' Booty")
					.addLore(C.mBody + "Old Chests Received: " + C.cYellow + _treasurePurchasedShop.get(TreasureType.OLD))
					.addLore(C.mBody + "Ancient Chests Received: " + C.cYellow + _treasurePurchasedShop.get(TreasureType.ANCIENT))
					.addLore(C.mBody + "Mythical Chests Received: " + C.cYellow + _treasurePurchasedShop.get(TreasureType.MYTHICAL))
					.build());
	}
}
