package mineplex.game.nano.game.components.compass;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.nano.game.components.team.GameTeam;

public class GameCompassPage extends ShopPageBase<GameCompassComponent, GameCompassShop>
{

	GameCompassPage(GameCompassComponent plugin, GameCompassShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Tracking Compass", player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int slot = 0;

		for (GameTeam team : getPlugin().getGame().getTeams())
		{
			for (Player target : team.getAlivePlayers())
			{
				if (slot >= getSize())
				{
					return;
				}

				addButton(slot++, new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
						.setTitle(team.getChatColour() + target.getName())
						.setPlayerHead(target.getName())
						.addLore(
								"",
								"Distance: " + C.cWhite + (int) UtilMath.offset(getPlayer(), target),
								"",
								"Click to teleport to " + target.getName() + "!"
						)
						.build(), (player, clickType) ->
				{
					player.closeInventory();
					player.teleport(target);
				});
			}
		}
	}
}
