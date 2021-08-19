package mineplex.staffServer.ui.rank;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilColor;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.treasure.types.TreasureType;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportRankListPage extends SupportPage
{
	private static int _rowOffset = 1;

	private Map<PermissionGroup, List<Pair<TreasureType, Integer>>> _rankBonusItems;

	public SupportRankListPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "Ranks");

		_rankBonusItems = new LinkedHashMap<>();

		_rankBonusItems.put(PermissionGroup.ULTRA, Collections.singletonList(
				Pair.create(TreasureType.MYTHICAL, 1)
		));

		_rankBonusItems.put(PermissionGroup.HERO, Collections.singletonList(
				Pair.create(TreasureType.MYTHICAL, 2)
		));

		_rankBonusItems.put(PermissionGroup.LEGEND, Collections.singletonList(
				Pair.create(TreasureType.MYTHICAL, 3)
		));

		_rankBonusItems.put(PermissionGroup.TITAN, Collections.singletonList(
				Pair.create(TreasureType.MYTHICAL, 5)
		));

		_rankBonusItems.put(PermissionGroup.ETERNAL, Arrays.asList(
				Pair.create(TreasureType.MYTHICAL, 2),
				Pair.create(TreasureType.ILLUMINATED, 2),
				Pair.create(TreasureType.OMEGA, 1)
		));

		buildPage();
	}

	private ItemStack getIconItem(PermissionGroup group, String[] lore)
	{
		return new ShopItem(Material.WOOL, UtilColor.chatColorToWoolData(group.getColor()), group.getDisplay(true, true, true, true).toUpperCase(), lore, 1, true, true);
	}

	private ItemStack getIconItem(PermissionGroup group)
	{
		return getIconItem(group, new String[0]);
	}

	private ItemStack getApplyItem(PermissionGroup group)
	{
		return new ItemBuilder(Material.GLOWSTONE_DUST)
				.setTitle(C.cGreenB + "Apply Rank")
				.addLore(C.mBody + "Click to apply " + group.getDisplay(true, true, true, true).toUpperCase())
				.addLore(C.mBody + "as this user's" )
				.addLore(C.mBody + "primary rank.")
				.build();
	}

	private ItemStack getBonusItem(PermissionGroup group)
	{
		ItemBuilder builder = new ItemBuilder(Material.CHEST)
				.setTitle(C.cGreenB + "Give Rank Bonus Items")
				.addLore(C.mBody + "Click to give all")
				.addLore(C.mBody + "monthly bonus items")
				.addLore(C.mBody + "for this rank:");

		for (Pair<TreasureType, Integer> rankReward : _rankBonusItems.get(group))
		{
			builder.addLore("  " + C.cYellow + rankReward.getRight() + " " + rankReward.getLeft().getName());
		}

		return builder.build();
	}

	private void handleGroupSet(PermissionGroup group)
	{
		if (_target.inheritsFrom(PermissionGroup.ADMIN) || _target.inheritsFrom(PermissionGroup.CONTENT))
		{
			message("You can't set this user's rank!");
			return;
		}

		getPlugin().getClientManager().setPrimaryGroup(_target.getAccountId(), group, () ->
		{
			message("You have applied the rank " + group.getDisplay(true, true, true, true).toUpperCase() + C.mBody + " to " + C.cYellow + _target.getName());
			_target.setPrimaryGroup(group);
			refresh();
		});
	}

	private void addRank(int column, PermissionGroup group)
	{
		addItem(getSlotIndex(_rowOffset, column), getIconItem(group));

		addButton(getSlotIndex(_rowOffset + 1, column), getApplyItem(group), (p, c) -> handleGroupSet(group));

		addButton(getSlotIndex(_rowOffset + 2, column), getBonusItem(group), (p, c) ->
		{
			for (Pair<TreasureType, Integer> reward : _rankBonusItems.get(group))
			{
				getPlugin().getInventoryManager().addItemToInventoryForOffline((success) ->
				{
					if (success)
					{
						_player.sendMessage(F.main(getPlugin().getName(), "You gave " + C.cYellow + reward.getRight() + " " + reward.getLeft().getItemName() + C.mBody + " to " + C.cYellow + _target.getName()));
					}
					else
					{
						_player.sendMessage(F.main(getPlugin().getName(), "Could not award " + C.cYellow + reward.getRight() + " " + reward.getLeft().getName() + " to " + C.cYellow + _target.getName() + C.mBody + " at this time. Please try again later."));
					}
				}, _target.getAccountId(), reward.getLeft().getItemName(), reward.getRight());
			}
		});
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		Set<PermissionGroup> groupSet = _rankBonusItems.keySet();

		int col = 0;

		for (PermissionGroup group : groupSet)
		{
			addRank(col, group);

			col += 2;
		}

		addButton(getSlotIndex(5, 4), getIconItem(PermissionGroup.PLAYER, new String[] {
				C.mBody + "Click to reset",
				C.mBody + "the player's rank",
				C.mBody + "to default."
		}), (p, c) -> handleGroupSet(PermissionGroup.PLAYER));
	}
}
