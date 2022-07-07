package nautilus.game.arcade.game.games.build.gui.page;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.build.Build;
import nautilus.game.arcade.game.games.build.BuildData;
import nautilus.game.arcade.game.games.build.gui.OptionsShop;

public class TimePage extends ShopPageBase<ArcadeManager, OptionsShop>
{
	private Build _game;

	public TimePage(Build game, ArcadeManager plugin, OptionsShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Set Time", player, 18);
		_game = game;
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		final BuildData buildData = _game.getBuildData(getPlayer());

		if (buildData == null)
		{
			getPlayer().closeInventory();
			return;
		}

		for (int i = 0; i < 9; i++)
		{
			final int ticks = 3000 * i;
			boolean am = (ticks >= 0 && ticks < 6000) || (ticks >= 18000);
			int time = (6 + (ticks / 1000)) % 12;
			if (time == 0) time = 12;

			Material material = buildData.Time == ticks ? Material.WATCH : Material.INK_SACK;
			byte data = (byte) (buildData.Time == ticks ? 0 : 8);
			ShopItem item = new ShopItem(material, data, time + (am ? "am" : "pm"), null, 0, false, false);
			addButton(i, item, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					buildData.Time = ticks;
					buildPage();
				}
			});
		}

		addButton(9 + 4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(player, new OptionsPage(_game, getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
			}
		});
	}
}
