package mineplex.game.clans.clans.supplydrop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.supplydrop.SupplyDropManager.SupplyDropType;

public class SupplyDropPage extends ShopPageBase<SupplyDropManager, SupplyDropShop>
{
	public SupplyDropPage(SupplyDropManager plugin, SupplyDropShop shop, String name, Player player)
	{
		super(plugin, shop, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), name, player, 27);
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int[] slots = {12, 14};
		int i = 0;
		for (SupplyDropType type : SupplyDropType.values())
		{
			int owned = getPlugin().getAmountOwned(getPlayer(), type);
			int slot = slots[i++];
			SkinData buttonData = type == SupplyDropType.GILDED ? SkinData.CLANS_GILDED_SUPPLY_DROP : SkinData.CLANS_SUPPLY_DROP;
			List<String> buttonLore = new ArrayList<>();
			buttonLore.addAll(Arrays.asList(C.cYellow + "Open a Supply Drop containing powerful items!",
					C.cRed + " ",
					C.cGreen + ">Click to Activate<",
					C.cBlue + " ",
					C.cDAqua + "You own " + F.greenElem(String.valueOf(Math.max(owned, 0))) + C.cDAqua + " " + type.getDisplayName() + "s"
			));
			if (type == SupplyDropType.GILDED)
			{
				buttonLore.add(C.cGreen + " ");
				buttonLore.add(C.cYellow + "Gilded Supply Drops contain better items!");
			}
			addButton(slot, buttonData.getSkull(C.cRed + type.getDisplayName(), buttonLore), (player, clickType) ->
			{
				if (!Recharge.Instance.use(player, "Clans Box Click", 1000, false, false))
				{
					return;
				}
				if (owned < 1)
				{
					playDenySound(player);
					UtilPlayer.message(player, F.main(getPlugin().getName(), "You do not have enough of that Supply Drop! Purchase some at http://www.mineplex.com/shop!"));
					return;
				}
				if (getPlugin().hasActiveSupplyDrop())
				{
					playDenySound(player);
					UtilPlayer.message(player, F.main(getPlugin().getName(), "There is already a Supply Drop dropping! Try again later!"));
					return;
				}
				SupplyDropManager manager = getPlugin();
				player.closeInventory();
				manager.useSupplyDrop(player, type);
			});
		}
	}
}