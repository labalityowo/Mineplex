package mineplex.core.cosmetic.ui.page.gamemodifiers.moba;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilUI;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.cosmetic.ui.page.gamemodifiers.GameCosmeticCategoryPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadget;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadgetData;
import mineplex.core.shop.item.ShopItem;

public class HeroSkinCategoryPage extends GadgetPage
{

	private static final Map<String, SkinData> SKIN_DATA = new HashMap<>();

	private static SkinData getSkinItem(String name)
	{
		if (SKIN_DATA.containsKey(name))
		{
			return SKIN_DATA.get(name);
		}

		try
		{
			Field field = SkinData.class.getDeclaredField(name.toUpperCase());
			SkinData data = (SkinData) field.get(null);
			SKIN_DATA.put(name, data);
			return data;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private final GameCosmeticCategoryPage _previousMenu;

	public HeroSkinCategoryPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, GameCosmeticCategoryPage previousMenu)
	{
		super(plugin, shop, clientManager, donationManager, name, player);

		_previousMenu = previousMenu;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		Map<String, List<HeroSkinGadgetData>> skinData = HeroSkinGadget.getSkins();
		int[] slots = UtilUI.getIndicesFor(skinData.size(), 2);
		int index = 0;

		for (Entry<String, List<HeroSkinGadgetData>> entry : skinData.entrySet())
		{
			String name = entry.getKey();
			List<HeroSkinGadgetData> gadgetDataList = entry.getValue();
			SkinData data = getSkinItem(name);

			if (data == null)
			{
				continue;
			}

			int own = 0;
			int total = 0;
			for (HeroSkinGadgetData gadgetData : gadgetDataList)
			{
				if (gadgetData.getGadget().ownsGadget(getPlayer()))
				{
					own++;
				}
				total++;
			}

			ItemStack itemStack = data.getSkull(C.cGreenB + name, Arrays.asList(
					"",
					C.cWhite + "You own " + own + "/" + total,
					"",
					C.cGreen + "Left-Click to view " + name + "'s skins"
			));

			addButton(slots[index++], itemStack, (player, clickType) -> getShop().openPageForPlayer(player, new HeroSkinGadgetPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), name, getPlayer(), this, gadgetDataList)));
		}

		addButton(4, new ShopItem(Material.BED, C.cGreen + "Go Back", new String[0], 1, false), (player, clickType) -> getShop().openPageForPlayer(getPlayer(), _previousMenu));
	}
}
