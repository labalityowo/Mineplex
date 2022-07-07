package mineplex.core.preferences.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilUI;
import mineplex.core.preferences.PreferenceCategory;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.preferences.PreferencesManager.Perm;
import mineplex.core.shop.page.ShopPageBase;

public class PreferencesMainPage extends ShopPageBase<PreferencesManager, PreferencesShop>
{

	private static final int INV_SIZE_MAX = 54;
	private static final int INV_SIZE_MIN = 45;

	public PreferencesMainPage(PreferencesManager plugin, PreferencesShop shop, Player player)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), plugin.getName(), player, plugin.getClientManager().Get(player).hasPermission(Perm.VIEW_EXCLUSIVE_MENU) ? INV_SIZE_MAX : INV_SIZE_MIN);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		boolean exclusive = getSize() > INV_SIZE_MIN;

		List<PreferenceCategory> categories;

		if (exclusive)
		{
			categories = Arrays.asList(PreferenceCategory.values());
		}
		else
		{
			categories = Arrays.stream(PreferenceCategory.values())
					.filter(category -> category != PreferenceCategory.EXCLUSIVE)
					.collect(Collectors.toList());
		}

		int size = categories.size();
		int[] slots = UtilUI.getIndicesFor(size, 2, 4, 0);

		for (int i = 0; i < size; i++)
		{
			int slot = slots[i];
			PreferenceCategory category = categories.get(i);
			addButton(slot, category.getItem(), (player, clickType) -> getShop().openPageForPlayer(player, new PreferencesPage(getPlugin(), getShop(), player, this, category)));
		}
	}
}
