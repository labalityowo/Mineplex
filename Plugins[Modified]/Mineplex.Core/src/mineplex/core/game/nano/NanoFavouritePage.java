package mineplex.core.game.nano;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.page.ShopPageBase;

public class NanoFavouritePage extends ShopPageBase<NanoFavourite, NanoShop>
{

	NanoFavouritePage(NanoFavourite plugin, NanoShop shop, Player player)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), "Favorite Games", player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addButtonNoAction(4, new ItemBuilder(Material.NETHER_STAR)
				.setGlow(true)
				.setTitle(C.cPurple + "Favorite Games")
				.addLore("Setting games as your favorite will", "increase the chance of them being", "played in Nano Games lobbies.")
				.build());

		int slot = 10;
		List<NanoDisplay> games = getPlugin().Get(getPlayer());

		for (NanoDisplay display : NanoDisplay.values())
		{
			boolean favourite = games.contains(display);
			String lore = favourite ? "Click to remove " + F.name(display.getName()) + " from" : "Click to add " + F.name(display.getName()) + " to";

			addButton(slot, new ItemBuilder(display.getMaterial(), display.getMaterialData())
					.setGlow(favourite)
					.setTitle((favourite ? C.cGreenB : C.cYellowB) + display.getName())
					.addLore(lore, "your favorite games list.")
					.build(), (player, clickType) ->
			{
				if (!Recharge.Instance.use(player, getPlugin().getName(), 1000, false, false))
				{
					return;
				}

				getPlugin().setFavourite(success ->
				{
					if (success)
					{
						playAcceptSound(player);
						refresh();
					}
					else
					{
						playDenySound(player);
						player.closeInventory();
					}
				}, player, display, !favourite);
			});

			if (++slot % 9 == 8)
			{
				slot += 2;
			}
		}
	}
}
