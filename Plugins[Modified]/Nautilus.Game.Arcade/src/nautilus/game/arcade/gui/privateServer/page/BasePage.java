package nautilus.game.arcade.gui.privateServer.page;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import nautilus.game.arcade.managers.GameHostManager;

public abstract class BasePage extends ShopPageBase<ArcadeManager, PrivateServerShop>
{

	protected static final ItemStack PREVIOUS_PAGE = new ItemBuilder(Material.ARROW)
			.setTitle(C.cGreen + "Previous Page")
			.build();
	protected static final ItemStack NEXT_PAGE = new ItemBuilder(Material.ARROW)
			.setTitle(C.cGreen + "Next Page")
			.build();
	protected static final int GAMES_PER_PAGE = 36;

	protected final GameHostManager _manager;

	public BasePage(ArcadeManager plugin, PrivateServerShop shop, String pageName, Player player)
	{
		this(plugin, shop, pageName, player, 54);
	}

	public BasePage(ArcadeManager plugin, PrivateServerShop shop, String pageName, Player player, int slots)
	{
		super(plugin, shop, plugin.GetClients(), plugin.GetDonation(), pageName, player, slots);

		_manager = plugin.GetGameHostManager();
	}

	public void addBackButton(int slot)
	{
		addButton(4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(player, new MenuPage(getPlugin(), getShop(), player));
			}
		});
	}

	public void addBackToSetGamePage()
	{
		addButton(4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(player, new SetGamePage(getPlugin(), getShop(), player));
			}
		});
	}

	public ItemStack getPlayerHead(String playerName, String title)
	{
		return getPlayerHead(playerName, title, null);
	}

	public ItemStack getPlayerHead(String playerName, String title, String[] lore)
	{
		ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

		SkullMeta meta = ((SkullMeta) is.getItemMeta());
		meta.setOwner(playerName);
		meta.setDisplayName(title);
		if (lore != null)
			meta.setLore(Arrays.asList(lore));
		is.setItemMeta(meta);

		return is;
	}
}
