package mineplex.core.achievement.leveling.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.achievement.leveling.ui.LevelRewardShop;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;

public class LevelRewardButton implements IButton
{

	private final LevelingManager _plugin;
	private final ShopPageBase<LevelingManager, LevelRewardShop> _menu;
	private final int _level;

	public LevelRewardButton(ShopPageBase<LevelingManager, LevelRewardShop> menu, int level)
	{
		_plugin = menu.getPlugin();
		_menu = menu;
		_level = level;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (_plugin.hasClaimed(player, _level) || !_plugin.canClaim(player, _level) || !Recharge.Instance.use(player, "Claim Level Reward", 250, true, false))
		{
			_menu.playDenySound(player);
			return;
		}

		_plugin.claim(player, _level, _menu);

		// Give some feedback
		_menu.playAcceptSound(player);

		// Rebuild the menu
		_menu.refresh();
	}
}