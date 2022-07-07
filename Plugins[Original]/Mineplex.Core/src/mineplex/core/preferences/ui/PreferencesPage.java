package mineplex.core.preferences.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilUI;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferenceCategory;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.preferences.UserPreferences;
import mineplex.core.shop.page.ShopPageBase;

public class PreferencesPage extends ShopPageBase<PreferencesManager, PreferencesShop>
{

	private static final ItemStack GO_BACK = new ItemBuilder(Material.BED)
			.setTitle(C.cGreen + "Go Back")
			.build();

	private final PreferencesMainPage _previous;
	private final PreferenceCategory _category;

	PreferencesPage(PreferencesManager plugin, PreferencesShop shop, Player player, PreferencesMainPage previous, PreferenceCategory category)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), category.getName(), player);

		_previous = previous;
		_category = category;
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<Preference> preferences = Preference.getByCategory(_category).stream()
				.filter(preference -> getPlugin().getClientManager().Get(getPlayer()).hasPermission(preference))
				.collect(Collectors.toList());
		UserPreferences user = getPlugin().get(getPlayer());

		int[] slots = UtilUI.getIndicesFor(preferences.size(), 1, 4, 1);

		for (int i = 0; i < preferences.size(); i++)
		{
			int slot = slots[i];
			Preference preference = preferences.get(i);
			boolean active = user.isActive(preference);

			ItemBuilder builder = new ItemBuilder(preference.getIcon())
					.setTitle(C.cRed + preference.getName())
					.setData(preference.getIcon() == Material.SKULL_ITEM ? (byte) 3 : 0);

			addToggleLore(builder, active);
			builder.addLore(preference.getLore());

			addButton(slot, builder.build(), (player, clickType) ->
			{
				playAcceptSound(player);
				user.toggle(preference);
				refresh();
			});

			builder.setType(Material.INK_SACK);
			builder.setData(active ? DyeColor.LIME.getDyeData() : DyeColor.GRAY.getDyeData());
			builder.getLore().clear();
			addToggleLore(builder, active);

			addButton(slot + 9, builder.build(), (player, clickType) ->
			{
				playAcceptSound(player);
				user.toggle(preference);
				refresh();
			});
		}

		addButton(4, GO_BACK, (player, clickType) -> getShop().openPageForPlayer(player, _previous));
	}

	private void addToggleLore(ItemBuilder builder, boolean active)
	{
		if (active)
		{
			builder.addLore(C.cGreen + "Enabled", "", C.cYellow + "Click to Disable");
		}
		else
		{
			builder.addLore(C.cRed + "Disabled", "", C.cYellow + "Click to Enable");
		}
	}

	@Override
	public void playerClosed()
	{
		getPlugin().save(getPlugin().get(getPlayer()));

		super.playerClosed();
	}
}
