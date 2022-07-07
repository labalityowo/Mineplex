package mineplex.core.game.kit.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;

abstract class KitPage extends ShopPageBase<MineplexGameManager, KitShop>
{

	private static final ItemStack GO_BACK = new ItemBuilder(Material.BED)
			.setTitle(C.cGreen + "Go Back")
			.build();

	protected final GameKit _kit;

	KitPage(MineplexGameManager plugin, Player player, GameKit kit, String name)
	{
		this(plugin, player, kit, name, 29);
	}

	KitPage(MineplexGameManager plugin, Player player, GameKit kit, String name, int size)
	{
		super(plugin, plugin.getShop(), plugin.getClientManager(), plugin.getDonationManager(), name, player, size);

		_kit = kit;
	}

	protected void addBackButton()
	{
		addButton(4, GO_BACK, (player, clickType) -> _plugin.openKitUI(player, _kit));
	}
}

