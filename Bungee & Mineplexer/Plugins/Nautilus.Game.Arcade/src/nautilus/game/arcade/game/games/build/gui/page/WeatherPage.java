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

public class WeatherPage extends ShopPageBase<ArcadeManager, OptionsShop>
{
	private Build _game;

	public WeatherPage(Build game, ArcadeManager plugin, OptionsShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Set Weather", player, 18);
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

		int sunnySlot = 2;
		int rainingSlot = 4;
		int stormingSlot = 6;

		ShopItem sunny = new ShopItem(Material.DOUBLE_PLANT, "Sunny", 1, false);
		ShopItem raining = new ShopItem(Material.WATER_BUCKET, "Raining", 1, false);
		ShopItem storming = new ShopItem(Material.GOLD_NUGGET, "Storming", 1, false);

		addButton(sunnySlot, sunny, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				buildData.Weather = BuildData.WeatherType.SUNNY;
				buildPage();
			}
		});

		addButton(rainingSlot, raining, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				buildData.Weather = BuildData.WeatherType.RAINING;
				buildPage();
			}
		});

		addButton(stormingSlot, storming, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				buildData.Weather = BuildData.WeatherType.STORMING;
				buildPage();
			}
		});

		switch (buildData.Weather)
		{
			case RAINING:
				addGlow(rainingSlot);
				break;
			case STORMING:
				addGlow(stormingSlot);
				break;
			default:
				addGlow(sunnySlot);
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
