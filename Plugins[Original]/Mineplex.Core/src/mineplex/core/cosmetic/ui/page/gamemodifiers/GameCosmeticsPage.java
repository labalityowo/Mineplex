package mineplex.core.cosmetic.ui.page.gamemodifiers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilUI;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticType;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;

public class GameCosmeticsPage extends GadgetPage
{

	public GameCosmeticsPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Game Cosmetics", player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		GameCosmeticManager manager = getPlugin().getGadgetManager().getGameCosmeticManager();
		Map<GameCosmeticType, List<GameCosmeticCategory>> cosmetics = manager.getTypes();
		int[] slots = UtilUI.getIndicesFor(cosmetics.size(), 2);
		int index = 0;

		for (Entry<GameCosmeticType, List<GameCosmeticCategory>> entry : cosmetics.entrySet())
		{
			int own = 0;
			int total = 0;

			for (GameCosmeticCategory category : entry.getValue())
			{
				for (GameModifierGadget gadget : category.getGadgets())
				{
					if (gadget.ownsGadget(getPlayer()))
					{
						own++;
					}
					total++;
				}
			}

			GameDisplay display = entry.getKey().getGame();
			ItemBuilder builder = new ItemBuilder(display.getMaterial(), display.getMaterialData());

			builder.setTitle(C.cGreenB + display.getName());
			builder.addLore(
					"",
					C.cWhite + "You own " + own + "/" + total,
					"",
					C.cGreen + "Left-Click to view " + display.getName() + "'s cosmetics"
			);

			addButton(slots[index++], builder.build(), (player, clickType) -> getShop().openPageForPlayer(player, new GameCosmeticCategoryPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), player, entry.getKey(), entry.getValue())));
		}

		addBackButton();
	}

}
