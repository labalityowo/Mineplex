package nautilus.game.pvp.modules.Benefit.UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mineplex.core.CurrencyType;
import mineplex.core.common.util.C;
import mineplex.core.Rank;
import me.chiss.Core.Shop.salespackage.ShopItem;
import me.chiss.Core.Shopv2.page.ConfirmationPage;
import me.chiss.Core.Shopv2.page.ShopPageBase;
import nautilus.game.pvp.modules.Benefit.BenefitManager;
import nautilus.game.pvp.modules.Benefit.BenefitShop;
import nautilus.game.pvp.modules.Benefit.Items.BenefitItem;
import nautilus.game.pvp.modules.Benefit.Items.CoinPack;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BenefitShopPage extends ShopPageBase<BenefitManager, BenefitShop>
{
	public BenefitShopPage(BenefitManager plugin, BenefitShop shop, Player player)
	{
		super(plugin, shop, "           Benefit Shop", player);
		BuildPage();
	}

	@SuppressWarnings("incomplete-switch")
	protected void BuildPage()
	{
		int slot = 53;
		boolean locked = false;
		
		for (BenefitItem item : Plugin.GetBenefitItems())
		{
			switch (item.GetDisplayMaterial())
			{
				case GOLD_INGOT:
					slot = 30;
					break;
				case GOLD_BLOCK:
					slot = 32;
					break;
				case DIAMOND_PICKAXE:
					slot = 40;
					
					if (!Client.Rank().Has(Rank.EMERALD, false))
						locked = true;
					
					break;
			}

			List<String> itemLore = new ArrayList<String>();

			StringBuilder currencyBuilder = new StringBuilder();

			for (CurrencyType currencyType : CurrencyType.values())
			{
				int cost = item.GetCost(currencyType);

				if (cost > 0)
				{
					currencyBuilder.append(C.cYellow + cost
							+ currencyType.Prefix() + ChatColor.WHITE + " or ");
				}
			}

			if (currencyBuilder.indexOf("or") != -1)
				itemLore.add(currencyBuilder.substring(0,  currencyBuilder.length() - 6));
			else
				itemLore.add(currencyBuilder.toString());
			
			itemLore.add(C.cBlack);

			itemLore.addAll(Arrays.asList(item.GetDescription()));

			ShopItem shopItem = new ShopItem(item.GetDisplayMaterial(),
					(byte) 0, item.getName(),
					itemLore.toArray(new String[itemLore.size()]), 1, locked,
					false);

			if (item instanceof CoinPack)
				AddButton(slot, shopItem, new CoinPackButton(this,
						((CoinPack) item)));
			else
				getInventory().setItem(slot, shopItem.getHandle());
		}
	}

	public void PurchaseCoinPack(Player player, CoinPack coinPack)
	{
		if (Client.Donor().GetBalance(SelectedCurrency, false) >= coinPack.GetCost(SelectedCurrency))
		{
			PlayAcceptSound(player);
			Shop.OpenPageForPlayer(player, new ConfirmationPage<BenefitManager, BenefitShop>(Plugin, Shop, Plugin.Repository,
					null, this, coinPack, SelectedCurrency, false, player));
		}
		else
		{
			PlayDenySound(player);
		}
	}
}
