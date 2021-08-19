package mineplex.core.achievement.leveling.ui.page;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.achievement.leveling.ui.LevelRewardShop;
import mineplex.core.achievement.leveling.ui.button.LevelRewardButton;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.MultiPageManager;
import mineplex.core.shop.page.ShopPageBase;

public class LevelRewardViewAllPage extends ShopPageBase<LevelingManager, LevelRewardShop>
{

	private static final ItemStack GO_BACK = new ItemBuilder(Material.BED)
			.setTitle(C.cGreen + "Go back")
			.build();

	private final ShopPageBase<LevelingManager, LevelRewardShop> _menu;
	private final MultiPageManager<Entry<Integer, List<LevelReward>>> _pageManager;

	LevelRewardViewAllPage(LevelingManager plugin, LevelRewardShop shop, ShopPageBase<LevelingManager, LevelRewardShop> menu, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "All Level Rewards", player);

		_menu = menu;
		_pageManager = new MultiPageManager<>(this, plugin::getSortedRewards, this::buildItem);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		_pageManager.buildPage();

		addButton(4, GO_BACK, (player, clickType) ->
		{
			_menu.refresh();
			_shop.openPageForPlayer(player, _menu);
		});
	}

	private void buildItem(Entry<Integer, List<LevelReward>> entry, int slot)
	{
		int level = entry.getKey();
		List<LevelReward> rewards = entry.getValue();
		ItemStack itemStack = _plugin.getLevelItem(_player, rewards, level);

		addButton(slot, itemStack, new LevelRewardButton(this, level));
	}

}
