package mineplex.core.preferences.ui;

import org.bukkit.entity.Player;

import mineplex.core.preferences.PreferencesManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class PreferencesShop extends ShopBase<PreferencesManager>
{

	public PreferencesShop(PreferencesManager plugin)
	{
		super(plugin, plugin.getClientManager(), plugin.getDonationManager(), "Preferences");
	}

	@Override
	protected ShopPageBase<PreferencesManager, ? extends ShopBase<PreferencesManager>> buildPagesFor(Player player)
	{
		return new PreferencesMainPage(getPlugin(), this, player);
	}
}
